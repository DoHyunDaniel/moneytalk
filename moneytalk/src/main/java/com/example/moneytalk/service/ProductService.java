package com.example.moneytalk.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.moneytalk.config.S3Uploader;
import com.example.moneytalk.domain.Ledger;
import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.ProductImage;
import com.example.moneytalk.domain.PurchaseHistory;
import com.example.moneytalk.domain.PurchaseRecord;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.ProductRequestDto;
import com.example.moneytalk.dto.ProductResponseDto;
import com.example.moneytalk.dto.ProductSearchRequest;
import com.example.moneytalk.repository.LedgerRepository;
import com.example.moneytalk.repository.ProductImageRepository;
import com.example.moneytalk.repository.ProductRepository;
import com.example.moneytalk.repository.PurchaseHistoryRepository;
import com.example.moneytalk.repository.PurchaseRecordRepository;
import com.example.moneytalk.type.LedgerType;
import com.example.moneytalk.type.ProductStatus;
import com.example.moneytalk.type.PurchaseType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	private final S3Uploader s3Uploader;
	private final PurchaseRecordRepository purchaseRecordRepository;
	private final PurchaseHistoryRepository purchaseHistoryRepository;
	private final LedgerRepository ledgerRepository;

	public ProductResponseDto createProduct(ProductRequestDto dto, User user) {
		Product product = Product.builder().user(user).title(dto.getTitle()).description(dto.getDescription())
				.price(dto.getPrice()).category(dto.getCategory()).location(dto.getLocation())
				.status(ProductStatus.SALE) // 기본값
				.build();

		Product saved = productRepository.save(product);

		return ProductResponseDto.builder().id(saved.getId()).title(saved.getTitle())
				.description(saved.getDescription()).price(saved.getPrice()).category(saved.getCategory())
				.location(saved.getLocation()).status(saved.getStatus()).createdAt(saved.getCreatedAt())
				.sellerNickname(saved.getUser().getNickname()).build();
	}

	public List<ProductResponseDto> getAllProducts() {
		List<Product> products = productRepository.findAllByOrderByCreatedAtDesc();

		return products.stream().map(product -> {
			List<String> imageUrls = productImageRepository.findByProduct(product).stream()
					.map(ProductImage::getImageUrl).toList();

			return ProductResponseDto.from(product, imageUrls);
		}).toList();
	}

	public ProductResponseDto getProductById(Long productId) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new IllegalArgumentException("해당 상품을 찾을 수 없습니다."));

		List<String> imageUrls = productImageRepository.findByProduct(product).stream().map(ProductImage::getImageUrl)
				.toList();

		return ProductResponseDto.builder().id(product.getId()).title(product.getTitle())
				.description(product.getDescription()).price(product.getPrice()).category(product.getCategory())
				.location(product.getLocation()).status(product.getStatus()).createdAt(product.getCreatedAt())
				.sellerNickname(product.getUser().getNickname()).images(imageUrls).build();
	}

	@Transactional
	public void updateProductStatus(Long productId, ProductStatus status, User user) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

		if (!product.getUser().getId().equals(user.getId())) {
			throw new AccessDeniedException("상품 수정 권한이 없습니다.");
		}

		product.setStatus(status);
	}

	@Transactional
	public void createProductWithImages(ProductRequestDto dto, List<MultipartFile> images, User user) {
		Product product = Product.builder().user(user).title(dto.getTitle()).description(dto.getDescription())
				.price(dto.getPrice()).category(dto.getCategory()).location(dto.getLocation())
				.status(ProductStatus.SALE).build();

		productRepository.save(product);

		if (images != null && !images.isEmpty()) {
			List<ProductImage> imageEntities = images.stream().map(file -> {
				String url = s3Uploader.uploadFile(file, "products");
				return ProductImage.builder().product(product).imageUrl(url).build();
			}).toList();
			productImageRepository.saveAll(imageEntities);
		}
	}

	public List<ProductResponseDto> searchProducts(ProductSearchRequest request) {
		List<Product> products = productRepository.searchByConditions(request);

		return products.stream().map(product -> {
			List<String> imageUrls = productImageRepository.findByProduct(product).stream()
					.map(ProductImage::getImageUrl).toList();
			return ProductResponseDto.from(product, imageUrls);
		}).toList();
	}

	@Transactional
	public void confirmPurchase(Long productId, User buyer) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

		if (product.getStatus() == ProductStatus.SOLD) {
			throw new IllegalStateException("이미 판매완료된 상품입니다.");
		}

		if (product.getUser().getId().equals(buyer.getId())) {
			throw new AccessDeniedException("본인의 상품은 구매 확정할 수 없습니다.");
		}

		if (!purchaseRecordRepository.existsByProduct(product)) {
			// 1. 구매 기록 테이블에 저장
			purchaseRecordRepository.save(PurchaseRecord.builder().product(product).buyer(buyer).build());
		}

		// 2. 상품 상태 및 구매자 설정
		product.setBuyer(buyer);
		product.setStatus(ProductStatus.SOLD);

		// 3. 구매/판매 이력 기록
		purchaseHistoryRepository
				.save(PurchaseHistory.builder().user(buyer).product(product).type(PurchaseType.PURCHASE).build());

		purchaseHistoryRepository.save(PurchaseHistory.builder().user(product.getUser()) // 판매자
				.product(product).type(PurchaseType.SALE).build());

		// 4. 가계부 기록 저장
		LocalDate today = LocalDate.now();
		int amount = product.getPrice();
		String title = product.getTitle();

		ledgerRepository.save(Ledger.builder().user(buyer).type(LedgerType.EXPENSE).amount(amount).category("중고거래")
				.memo(title + " 구매").date(today).build());

		ledgerRepository.save(Ledger.builder().user(product.getUser()).type(LedgerType.INCOME).amount(amount)
				.category("중고거래").memo(title + " 판매").date(today).build());

		// 5. 구매/판매 이력 저장
		if (!purchaseHistoryRepository.existsByUserAndProductAndType(buyer, product, PurchaseType.PURCHASE)) {
			purchaseHistoryRepository
					.save(PurchaseHistory.builder().user(buyer).product(product).type(PurchaseType.PURCHASE).build());
		}

		if (!purchaseHistoryRepository.existsByUserAndProductAndType(product.getUser(), product, PurchaseType.SALE)) {
			purchaseHistoryRepository.save(
					PurchaseHistory.builder().user(product.getUser()).product(product).type(PurchaseType.SALE).build());
		}

		// 6. 가계부 기록 저장
		if (!ledgerRepository.existsByUserAndMemoAndAmount(buyer, title + " 구매", amount)) {
			ledgerRepository.save(Ledger.builder().user(buyer).type(LedgerType.EXPENSE).amount(amount).category("중고거래")
					.memo(title + " 구매").date(today).build());
		}

		if (!ledgerRepository.existsByUserAndMemoAndAmount(product.getUser(), title + " 판매", amount)) {
			ledgerRepository.save(Ledger.builder().user(product.getUser()).type(LedgerType.INCOME).amount(amount)
					.category("중고거래").memo(title + " 판매").date(today).build());
		}

	}

}
