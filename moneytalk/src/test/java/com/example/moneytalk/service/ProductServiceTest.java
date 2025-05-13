package com.example.moneytalk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import com.example.moneytalk.config.S3Uploader;
import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.ProductImage;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.ProductRequestDto;
import com.example.moneytalk.dto.ProductResponseDto;
import com.example.moneytalk.dto.ProductSearchRequestDto;
import com.example.moneytalk.exception.GlobalException;
import com.example.moneytalk.repository.LedgerRepository;
import com.example.moneytalk.repository.ProductImageRepository;
import com.example.moneytalk.repository.ProductRepository;
import com.example.moneytalk.repository.PurchaseHistoryRepository;
import com.example.moneytalk.repository.PurchaseRecordRepository;
import com.example.moneytalk.type.ErrorCode;
import com.example.moneytalk.type.ProductStatus;
import com.example.moneytalk.type.PurchaseType;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private ProductImageRepository productImageRepository;

	@Mock
	private PurchaseRecordRepository purchaseRecordRepository;

	@Mock
	private PurchaseHistoryRepository purchaseHistoryRepository;

	@Mock
	private LedgerRepository ledgerRepository;

	@Mock
	private S3Uploader s3Uploader;

	@InjectMocks
	private ProductService productService;

	@Test
	void createProduct_정상등록_성공() {
		// given
		ProductRequestDto requestDto = new ProductRequestDto();
		requestDto.setTitle("맥북 에어 M2");
		requestDto.setDescription("미개봉 새제품입니다.");
		requestDto.setPrice(1500000);
		requestDto.setCategory("전자기기");
		requestDto.setLocation("서울");

		User user = User.builder().id(1L).nickname("판매왕").email("test@example.com").password("secure123").build();

		Product savedProduct = Product.builder().id(100L).user(user).title(requestDto.getTitle())
				.description(requestDto.getDescription()).price(requestDto.getPrice())
				.category(requestDto.getCategory()).location(requestDto.getLocation()).status(requestDto.getStatus())
				.build();

		given(productRepository.save(any(Product.class))).willReturn(savedProduct);

		// when
		ProductResponseDto response = productService.createProduct(requestDto, user);

		// then
		assertThat(response.getId()).isEqualTo(100L);
		assertThat(response.getTitle()).isEqualTo("맥북 에어 M2");
		assertThat(response.getPrice()).isEqualTo(1500000);
		assertThat(response.getSellerNickname()).isEqualTo("판매왕");
	}

	@Test
	void getAllProducts_최신순조회_성공() {
		// given
		User user = User.builder().id(1L).nickname("판매왕").email("test@example.com").build();

		Product product1 = Product.builder().id(1L).title("아이폰 13").description("중고, 양호").price(800000).category("전자기기")
				.location("서울").status(ProductStatus.SALE).user(user).build();

		Product product2 = Product.builder().id(2L).title("갤럭시 S23").description("미개봉").price(1000000).category("전자기기")
				.location("서울").status(ProductStatus.SALE).user(user).build();

		List<Product> mockProducts = List.of(product2, product1); // 최신순 가정

		given(productRepository.findAllByOrderByCreatedAtDesc()).willReturn(mockProducts);
		given(productImageRepository.findByProduct(any(Product.class)))
				.willReturn(List.of(ProductImage.builder().imageUrl("https://img.com/1.jpg").build()));

		// when
		List<ProductResponseDto> result = productService.getAllProducts();

		// then
		assertThat(result).hasSize(2);

		assertThat(result.get(0).getTitle()).isEqualTo("갤럭시 S23");
		assertThat(result.get(0).getSellerNickname()).isEqualTo("판매왕");
		assertThat(result.get(0).getImages()).contains("https://img.com/1.jpg");

		assertThat(result.get(1).getTitle()).isEqualTo("아이폰 13");
		assertThat(result.get(1).getSellerId()).isEqualTo(1L);
	}

	@Test
	void getProductById_정상조회_성공() {
		// given
		Long productId = 1L;
		User user = User.builder().id(1L).nickname("테스터").build();

		Product product = Product.builder().id(productId).title("아이패드").description("최신형").price(700000)
				.category("전자기기").location("부산").status(ProductStatus.SALE).user(user).build();

		given(productRepository.findWithUserById(productId)).willReturn(Optional.of(product));
		given(productImageRepository.findByProduct(product))
				.willReturn(List.of(ProductImage.builder().imageUrl("https://img.com/ipad.jpg").build()));

		// when
		ProductResponseDto result = productService.getProductById(productId);

		// then
		assertThat(result.getId()).isEqualTo(productId);
		assertThat(result.getTitle()).isEqualTo("아이패드");
		assertThat(result.getSellerNickname()).isEqualTo("테스터");
		assertThat(result.getImages()).contains("https://img.com/ipad.jpg");
	}

	@Test
	void getProductById_상품없음_예외발생() {
		// given
		Long productId = 999L;
		given(productRepository.findWithUserById(productId)).willReturn(Optional.empty());

		// when
		Throwable thrown = catchThrowable(() -> productService.getProductById(productId));

		// then
		assertThat(thrown).isInstanceOf(GlobalException.class);
		assertThat(((GlobalException) thrown).getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
	}

	@Test
	void updateProductStatus_본인상품_상태변경_성공() {
		// given
		Long productId = 1L;
		User seller = User.builder().id(1L).nickname("판매자").build();
		Product product = Product.builder().id(productId).title("아이폰 13").status(ProductStatus.SALE).user(seller)
				.build();

		given(productRepository.findById(productId)).willReturn(Optional.of(product));

		// when
		productService.updateProductStatus(productId, ProductStatus.RESERVED, seller);

		// then
		assertThat(product.getStatus()).isEqualTo(ProductStatus.RESERVED);
	}

	@Test
	void updateProductStatus_상품없음_예외() {
		// given
		Long productId = 999L;
		User user = User.builder().id(1L).build();

		given(productRepository.findById(productId)).willReturn(Optional.empty());

		// when
		Throwable thrown = catchThrowable(
				() -> productService.updateProductStatus(productId, ProductStatus.SOLD, user));

		// then
		assertThat(thrown).isInstanceOf(GlobalException.class);
		assertThat(((GlobalException) thrown).getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
	}

	@Test
	void updateProductStatus_타인상품_접근거부_예외() {
		// given
		Long productId = 1L;
		User seller = User.builder().id(1L).nickname("판매자").build();
		User otherUser = User.builder().id(2L).nickname("침입자").build();

		Product product = Product.builder().id(productId).title("아이폰 13").status(ProductStatus.SALE).user(seller)
				.build();

		given(productRepository.findById(productId)).willReturn(Optional.of(product));

		// when
		Throwable thrown = catchThrowable(
				() -> productService.updateProductStatus(productId, ProductStatus.SOLD, otherUser));

		// then
		assertThat(thrown).isInstanceOf(GlobalException.class);
		assertThat(((GlobalException) thrown).getErrorCode()).isEqualTo(ErrorCode.PRODUCT_ACCESS_DENIED);
	}

	@Test
	void createProductWithImages_정상등록_성공() {
		// given
		ProductRequestDto dto = new ProductRequestDto();
		dto.setTitle("카메라");
		dto.setDescription("중고, 양호");
		dto.setPrice(300000);
		dto.setCategory("전자기기");
		dto.setLocation("서울");

		User user = User.builder().id(1L).nickname("판매자").build();

		// 모의 이미지 파일 1개 생성
		MockMultipartFile image = new MockMultipartFile("image", "camera.jpg", "image/jpeg",
				"fake-image-content".getBytes());

		List<MultipartFile> images = List.of(image);

		Product dummySavedProduct = Product.builder().id(1L).user(user).title(dto.getTitle())
				.description(dto.getDescription()).price(dto.getPrice()).category(dto.getCategory())
				.location(dto.getLocation()).status(ProductStatus.SALE).build();

		given(productRepository.save(any(Product.class))).willReturn(dummySavedProduct);
		given(s3Uploader.uploadFile(any(MultipartFile.class), eq("products")))
				.willReturn("https://s3.amazon.com/products/camera.jpg");

		// when
		productService.createProductWithImages(dto, images, user);

		// then
		// 이미지 저장이 호출되었는지 검증
		verify(productImageRepository, times(1)).saveAll(any());
	}

	@Test
	void searchProducts_조건검색_성공() {
		// given
		ProductSearchRequestDto request = new ProductSearchRequestDto(); // 조건 없이도 호출 가능

		User user = User.builder().id(1L).nickname("판매자").build();

		Product product = Product.builder().id(1L).title("닌텐도 스위치").description("박스 포함, 상태 A급").price(250000)
				.category("게임기기").location("서울").status(ProductStatus.SALE).user(user).build();

		given(productRepository.searchByConditions(any(ProductSearchRequestDto.class))).willReturn(List.of(product));

		given(productImageRepository.findByProduct(product))
				.willReturn(List.of(ProductImage.builder().imageUrl("https://s3.amazon.com/nintendo.jpg").build()));

		// when
		List<ProductResponseDto> result = productService.searchProducts(request);

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getTitle()).isEqualTo("닌텐도 스위치");
		assertThat(result.get(0).getImages()).contains("https://s3.amazon.com/nintendo.jpg");
		assertThat(result.get(0).getSellerNickname()).isEqualTo("판매자");
	}

	@Test
	void confirmPurchase_성공() {
		// given
		Long productId = 1L;

		User seller = User.builder().id(1L).nickname("판매자").build();
		User buyer = User.builder().id(2L).nickname("구매자").build();

		Product product = Product.builder().id(productId).user(seller).price(10000).title("책상").category("가구")
				.location("서울").status(ProductStatus.SALE).build();

		given(productRepository.findById(productId)).willReturn(Optional.of(product));
		given(purchaseRecordRepository.existsByProduct(product)).willReturn(false);
		given(purchaseHistoryRepository.existsByUserAndProductAndType(eq(buyer), eq(product),
				eq(PurchaseType.PURCHASE))).willReturn(false);
		given(purchaseHistoryRepository.existsByUserAndProductAndType(eq(seller), eq(product), eq(PurchaseType.SALE)))
				.willReturn(false);
		given(ledgerRepository.existsByUserAndMemoAndAmount(eq(buyer), eq("책상 구매"), eq(10000))).willReturn(false);
		given(ledgerRepository.existsByUserAndMemoAndAmount(eq(seller), eq("책상 판매"), eq(10000))).willReturn(false);

		// when
		productService.confirmPurchase(productId, buyer);

		// then
		assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD);
		assertThat(product.getBuyer()).isEqualTo(buyer);

		// Mock 저장 메소드들 모두 1회씩 호출됐는지 확인
		verify(purchaseRecordRepository).save(any());
		verify(purchaseHistoryRepository, times(4)).save(any()); // 구매자 + 판매자
		verify(ledgerRepository, times(4)).save(any()); // 수입 + 지출
	}

	@Test
	void confirmPurchase_실패_상품없음() {
		// given
		Long productId = 999L;
		User buyer = User.builder().id(1L).build();

		given(productRepository.findById(productId)).willReturn(Optional.empty());

		// when & then
		GlobalException ex = assertThrows(GlobalException.class,
				() -> productService.confirmPurchase(productId, buyer));
		assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);
	}


}
