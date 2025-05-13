package com.example.moneytalk.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.example.moneytalk.config.S3Uploader;
import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.Review;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.repository.ProductRepository;
import com.example.moneytalk.repository.ReviewRepository;
import com.example.moneytalk.repository.UserRepository;
import com.example.moneytalk.type.ProductStatus;
import com.example.moneytalk.type.UserType;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewControllerTest {

	@MockBean
	private AmazonS3 amazonS3;
	@MockBean
	private S3Uploader s3Uploader;

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private ReviewRepository reviewRepository;

	private User buyer;
	private User reviewee;
	private Product product;

	private static String unique(String prefix) {
		return prefix + "_" + UUID.randomUUID().toString().substring(0, 6);
	}

	@BeforeEach
	void setup() {
		buyer = userRepository.save(User.builder().email(unique("buyer") + "@example.com").password("encodedPassword")
				.nickname(unique("구매자")).role(UserType.USER).build());

		reviewee = userRepository.save(User.builder().email(unique("seller") + "@example.com")
				.password("encodedPassword").nickname(unique("판매자")).role(UserType.USER).build());

		product = productRepository.save(Product.builder().title("테스트 상품").description("설명").price(10000)
				.category("전자기기").location("서울").status(ProductStatus.SOLD).user(reviewee).buyer(buyer).build());

		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(buyer, null, buyer.getAuthorities()));

		when(s3Uploader.uploadFile(any(MultipartFile.class), anyString()))
				.thenReturn("https://mock-s3-url.com/test.jpg");
	}

	@Test
	void 리뷰작성_정상요청_성공() throws Exception {
		MockMultipartFile image1 = new MockMultipartFile("images", "image1.jpg", MediaType.IMAGE_JPEG_VALUE,
				"img1".getBytes());
		MockMultipartFile image2 = new MockMultipartFile("images", "image2.jpg", MediaType.IMAGE_JPEG_VALUE,
				"img2".getBytes());

		mockMvc.perform(multipart("/api/reviews").file(image1).file(image2)
				.param("productId", product.getId().toString()).param("revieweeId", reviewee.getId().toString())
				.param("rating", "5").param("content", "정말 만족스러운 거래였습니다.").contentType(MediaType.MULTIPART_FORM_DATA)
				.characterEncoding("UTF-8")).andExpect(status().isOk());
	}

	@Test
	void 리뷰수정_정상요청_성공() throws Exception {
		User reviewer = userRepository.save(User.builder().email(unique("reviewer") + "@example.com")
				.password("encoded").nickname(unique("리뷰어")).role(UserType.USER).build());

		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(reviewer, null, reviewer.getAuthorities()));

		User reviewee = userRepository.save(User.builder().email(unique("target") + "@example.com").password("test")
				.nickname(unique("판매자")).role(UserType.USER).build());

		Product product = productRepository.save(Product.builder().title("상품").description("설명").price(10000)
				.category("기타").location("서울").status(ProductStatus.SOLD).user(reviewee).buyer(reviewer).build());

		Review review = reviewRepository.save(Review.builder().product(product).reviewer(reviewer).reviewee(reviewee)
				.rating(3).content("처음 내용").build());

		MockMultipartFile newImage = new MockMultipartFile("images", "image.jpg", MediaType.IMAGE_JPEG_VALUE,
				"img".getBytes());

		mockMvc.perform(multipart("/api/reviews/{reviewId}", review.getId()).file(newImage).param("rating", "5")
				.param("content", "수정된 내용입니다.").with(request -> {
					request.setMethod("PATCH");
					return request;
				}).contentType(MediaType.MULTIPART_FORM_DATA)).andExpect(status().isNoContent());
	}

	@Test
	void 리뷰삭제_정상요청_성공() throws Exception {
		User reviewer = userRepository.save(User.builder().email(unique("reviewer") + "@example.com")
				.password("encoded").nickname(unique("리뷰어")).role(UserType.USER).build());

		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(reviewer, null, reviewer.getAuthorities()));

		User reviewee = userRepository.save(User.builder().email(unique("target") + "@example.com").password("test")
				.nickname(unique("판매자")).role(UserType.USER).build());

		Product product = productRepository.save(Product.builder().title("삭제용 상품").description("삭제 테스트").price(5000)
				.category("책").location("부산").status(ProductStatus.SOLD).user(reviewee).buyer(reviewer).build());

		Review review = reviewRepository.save(Review.builder().product(product).reviewer(reviewer).reviewee(reviewee)
				.rating(4).content("삭제할 리뷰").build());

		mockMvc.perform(delete("/api/reviews/{reviewId}", review.getId())).andExpect(status().isNoContent());
	}

	@Test
	void 리뷰작성_중복작성_예외() throws Exception {
		// 리뷰 선작성
		reviewRepository.save(Review.builder().product(product).reviewer(buyer).reviewee(reviewee).rating(4)
				.content("기존 리뷰").build());

		// 이미지 파일
		MockMultipartFile image = new MockMultipartFile("images", "image1.jpg", MediaType.IMAGE_JPEG_VALUE,
				"data".getBytes());

		// 동일 상품/리뷰이로 중복 리뷰 작성 시도
		mockMvc.perform(multipart("/api/reviews").file(image).param("productId", product.getId().toString())
				.param("revieweeId", reviewee.getId().toString()).param("rating", "5").param("content", "중복 작성 시도")
				.contentType(MediaType.MULTIPART_FORM_DATA).characterEncoding("UTF-8")).andExpect(status().isConflict())
				.andExpect(jsonPath("$.error").value("REVIEW_ALREADY_WRITTEN"))
				.andExpect(jsonPath("$.message").value("이미 리뷰를 작성한 상품입니다."));
	}

	@Test
	void 리뷰작성_상품없음_예외() throws Exception {
		// given
		Long invalidProductId = 9999L; // 존재하지 않는 ID

		MockMultipartFile image = new MockMultipartFile("images", "image.jpg", MediaType.IMAGE_JPEG_VALUE,
				"img".getBytes());

		// when & then
		mockMvc.perform(multipart("/api/reviews").file(image).param("productId", invalidProductId.toString())
				.param("revieweeId", reviewee.getId().toString()).param("rating", "4").param("content", "존재하지 않는 상품")
				.contentType(MediaType.MULTIPART_FORM_DATA).characterEncoding("UTF-8")).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("존재하지 않는 상품입니다."));
	}

	@Test
	void 리뷰삭제_본인아님_예외() throws Exception {
		// 리뷰 작성자
		User reviewer = userRepository.save(User.builder().email(unique("reviewer") + "@example.com").password("pass")
				.nickname(unique("작성자")).role(UserType.USER).build());

		// 로그인 유저(삭제 시도자)
		User otherUser = userRepository.save(User.builder().email(unique("other") + "@example.com").password("pass")
				.nickname(unique("다른유저")).role(UserType.USER).build());

		// 상품과 리뷰 대상자
		Product product = productRepository.save(Product.builder().title("상품").description("설명").price(10000)
				.category("기타").location("서울").status(ProductStatus.SOLD).user(reviewee).buyer(reviewer).build());

		Review review = reviewRepository.save(Review.builder().product(product).reviewer(reviewer).reviewee(reviewee)
				.rating(3).content("리뷰").build());

		// 다른 유저로 로그인
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(otherUser, null, otherUser.getAuthorities()));

		mockMvc.perform(delete("/api/reviews/{reviewId}", review.getId())).andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error").value("REVIEW_DELETE_FORBIDDEN"))
				.andExpect(jsonPath("$.message").value("본인의 리뷰만 삭제할 수 있습니다."));
	}

	@Test
	void 리뷰수정_본인아님_예외() throws Exception {
		// 리뷰 작성자
		User reviewer = userRepository.save(User.builder().email(unique("reviewer") + "@example.com").password("pass")
				.nickname(unique("작성자")).role(UserType.USER).build());

		// 리뷰 대상자
		User reviewee = userRepository.save(User.builder().email(unique("reviewee") + "@example.com").password("pass")
				.nickname(unique("판매자")).role(UserType.USER).build());

		// 수정 시도자 (본인 아님)
		User attacker = userRepository.save(User.builder().email(unique("attacker") + "@example.com").password("pass")
				.nickname(unique("침입자")).role(UserType.USER).build());

		// 상품
		Product product = productRepository.save(Product.builder().title("상품").description("설명").price(15000)
				.category("가전").location("서울").status(ProductStatus.SOLD).user(reviewee).buyer(reviewer).build());

		// 리뷰 저장 (작성자는 reviewer)
		Review review = reviewRepository.save(Review.builder().product(product).reviewer(reviewer).reviewee(reviewee)
				.rating(4).content("원본 내용").build());

		// 로그인: 본인이 아닌 attacker
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(attacker, null, attacker.getAuthorities()));

		MockMultipartFile image = new MockMultipartFile("images", "image.jpg", MediaType.IMAGE_JPEG_VALUE,
				"img".getBytes());

		// when & then
		mockMvc.perform(multipart("/api/reviews/{reviewId}", review.getId()).file(image).param("rating", "5")
				.param("content", "권한 없는 수정 시도").with(request -> {
					request.setMethod("PATCH");
					return request;
				}).contentType(MediaType.MULTIPART_FORM_DATA)).andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error").value("REVIEW_UPDATE_FORBIDDEN"))
				.andExpect(jsonPath("$.message").value("본인의 리뷰만 수정할 수 있습니다."));
	}

	@Test
	void 리뷰작성_상품상태가_SOLD아님_예외() throws Exception {
		// 구매자 (로그인 사용자)
		User buyer = userRepository.save(User.builder().email(unique("buyer") + "@example.com").password("pw")
				.nickname(unique("구매자")).role(UserType.USER).build());

		// 리뷰 대상자
		User reviewee = userRepository.save(User.builder().email(unique("seller") + "@example.com").password("pw")
				.nickname(unique("판매자")).role(UserType.USER).build());

		// 상품 상태가 SOLD가 아님 (e.g. SALE)
		Product product = productRepository.save(Product.builder().title("판매 중 상품").description("아직 안 팔림").price(7000)
				.category("기타").location("서울").status(ProductStatus.SALE) // 이게 포인트
				.user(reviewee).buyer(buyer).build());

		// 로그인 설정
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(buyer, null, buyer.getAuthorities()));

		MockMultipartFile image = new MockMultipartFile("images", "image.jpg", MediaType.IMAGE_JPEG_VALUE,
				"img".getBytes());

		// when & then
		mockMvc.perform(multipart("/api/reviews").file(image).param("productId", product.getId().toString())
				.param("revieweeId", reviewee.getId().toString()).param("rating", "4")
				.param("content", "아직 구매가 안 끝난 상품에 리뷰").contentType(MediaType.MULTIPART_FORM_DATA)
				.characterEncoding("UTF-8")).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("PRODUCT_NOT_SOLD_YET"))
				.andExpect(jsonPath("$.message").value("아직 구매가 확정되지 않은 상품입니다."));
	}

	@Test
	void 리뷰작성_구매자가아님_예외() throws Exception {
		// 로그인 사용자 (실제 구매자 아님)
		User otherUser = userRepository.save(User.builder().email(unique("other") + "@example.com").password("pw")
				.nickname(unique("다른사람")).role(UserType.USER).build());

		// 리뷰 대상자
		User reviewee = userRepository.save(User.builder().email(unique("seller") + "@example.com").password("pw")
				.nickname(unique("판매자")).role(UserType.USER).build());

		// 상품은 다른 사람이 구매한 것으로 설정
		User realBuyer = userRepository.save(User.builder().email(unique("realbuyer") + "@example.com").password("pw")
				.nickname(unique("진짜구매자")).role(UserType.USER).build());

		Product product = productRepository.save(Product.builder().title("테스트 상품").description("설명").price(10000)
				.category("도서").location("서울").status(ProductStatus.SOLD).user(reviewee).buyer(realBuyer) // 진짜 구매자
				.build());

		// 로그인 사용자 설정 (구매자 아님)
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(otherUser, null, otherUser.getAuthorities()));

		// 파일
		MockMultipartFile image = new MockMultipartFile("images", "img.jpg", MediaType.IMAGE_JPEG_VALUE,
				"test".getBytes());

		// when & then
		mockMvc.perform(multipart("/api/reviews").file(image).param("productId", product.getId().toString())
				.param("revieweeId", reviewee.getId().toString()).param("rating", "5").param("content", "구매자 아님")
				.contentType(MediaType.MULTIPART_FORM_DATA).characterEncoding("UTF-8"))
				.andExpect(status().isForbidden()).andExpect(jsonPath("$.error").value("REVIEW_WRITE_FORBIDDEN"))
				.andExpect(jsonPath("$.message").value("해당 상품을 구매한 사용자만 리뷰를 작성할 수 있습니다."));
	}

	@Test
	void 리뷰작성_자기자신에게_예외() throws Exception {
		// 구매자(로그인 사용자) == 리뷰 대상자 (자기 자신)
		User self = userRepository.save(User.builder().email(unique("self") + "@example.com").password("pw")
				.nickname(unique("자기자신")).role(UserType.USER).build());

		Product product = productRepository.save(Product.builder().title("본인 상품").description("테스트").price(5000)
				.category("잡화").location("서울").status(ProductStatus.SOLD).user(self) // 판매자
				.buyer(self) // 구매자 == 본인
				.build());

		// 로그인 사용자 설정
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(self, null, self.getAuthorities()));

		MockMultipartFile image = new MockMultipartFile("images", "img.jpg", MediaType.IMAGE_JPEG_VALUE,
				"test".getBytes());

		mockMvc.perform(multipart("/api/reviews").file(image).param("productId", product.getId().toString())
				.param("revieweeId", self.getId().toString()).param("rating", "3").param("content", "본인에게 리뷰 시도")
				.contentType(MediaType.MULTIPART_FORM_DATA).characterEncoding("UTF-8"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("CANNOT_REVIEW_SELF"))
				.andExpect(jsonPath("$.message").value("자기 자신에게는 리뷰를 작성할 수 없습니다."));
	}

	@Test
	void 리뷰수정_리뷰없음_예외() throws Exception {
		// 로그인 사용자 설정
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(buyer, null, buyer.getAuthorities()));

		Long nonExistentReviewId = 9999L;

		MockMultipartFile image = new MockMultipartFile("images", "image.jpg", MediaType.IMAGE_JPEG_VALUE,
				"img".getBytes());

		mockMvc.perform(multipart("/api/reviews/{reviewId}", nonExistentReviewId).file(image).param("rating", "5")
				.param("content", "없는 리뷰 수정 시도").with(request -> {
					request.setMethod("PATCH");
					return request;
				}).contentType(MediaType.MULTIPART_FORM_DATA).characterEncoding("UTF-8"))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.error").value("REVIEW_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("리뷰가 존재하지 않습니다."));
	}

	@Test
	void 리뷰삭제_리뷰없음_예외() throws Exception {
		// 로그인 사용자 설정
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(buyer, null, buyer.getAuthorities()));

		Long nonExistentReviewId = 9999L;

		mockMvc.perform(delete("/api/reviews/{reviewId}", nonExistentReviewId)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("REVIEW_NOT_FOUND"))
				.andExpect(jsonPath("$.message").value("리뷰가 존재하지 않습니다."));
	}

	@Test
	void 리뷰수정_다른사용자_예외() throws Exception {
		// 리뷰어 등록
		User reviewer = userRepository.save(User.builder().email(unique("realwriter") + "@test.com").password("encoded")
				.nickname(unique("리뷰작성자")).role(UserType.USER).build());

		// 리뷰 대상자
		User reviewee = userRepository.save(User.builder().email(unique("target") + "@test.com").password("test")
				.nickname(unique("판매자")).role(UserType.USER).build());

		// 상품
		Product product = productRepository.save(Product.builder().title("상품").description("설명").price(10000)
				.category("기타").location("서울").status(ProductStatus.SOLD).user(reviewee).buyer(reviewer).build());

		// 리뷰 저장
		Review review = reviewRepository.save(Review.builder().product(product).reviewer(reviewer).reviewee(reviewee)
				.rating(3).content("원본 리뷰").build());

		// 로그인 사용자: 리뷰 작성자가 아닌 다른 유저
		User otherUser = userRepository.save(User.builder().email(unique("intruder") + "@test.com").password("test")
				.nickname(unique("침입자")).role(UserType.USER).build());

		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(otherUser, null, otherUser.getAuthorities()));

		// 이미지 생성
		MockMultipartFile newImage = new MockMultipartFile("images", "image.jpg", MediaType.IMAGE_JPEG_VALUE,
				"img".getBytes());

		// PATCH 요청
		mockMvc.perform(multipart("/api/reviews/{reviewId}", review.getId()).file(newImage).param("rating", "1")
				.param("content", "침입자가 수정 시도").with(request -> {
					request.setMethod("PATCH");
					return request;
				}).contentType(MediaType.MULTIPART_FORM_DATA)).andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error").value("REVIEW_UPDATE_FORBIDDEN"))
				.andExpect(jsonPath("$.message").value("본인의 리뷰만 수정할 수 있습니다."));
	}

	@Test
	void 리뷰삭제_다른사용자_예외() throws Exception {
		// 실제 리뷰 작성자
		User reviewer = userRepository.save(User.builder().email(unique("realwriter") + "@test.com").password("encoded")
				.nickname(unique("리뷰작성자")).role(UserType.USER).build());

		// 리뷰 대상자
		User reviewee = userRepository.save(User.builder().email(unique("reviewee") + "@test.com").password("test")
				.nickname(unique("판매자")).role(UserType.USER).build());

		// 상품
		Product product = productRepository.save(Product.builder().title("상품").description("설명").price(10000)
				.category("기타").location("서울").status(ProductStatus.SOLD).user(reviewee).buyer(reviewer).build());

		// 리뷰 등록
		Review review = reviewRepository.save(Review.builder().product(product).reviewer(reviewer).reviewee(reviewee)
				.rating(5).content("삭제할 리뷰").build());

		// 로그인 유저: 리뷰 작성자가 아닌 제3자
		User otherUser = userRepository.save(User.builder().email(unique("intruder") + "@test.com").password("test")
				.nickname(unique("침입자")).role(UserType.USER).build());

		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(otherUser, null, otherUser.getAuthorities()));

		// 삭제 시도
		mockMvc.perform(delete("/api/reviews/{reviewId}", review.getId())).andExpect(status().isForbidden())
				.andExpect(jsonPath("$.error").value("REVIEW_DELETE_FORBIDDEN"))
				.andExpect(jsonPath("$.message").value("본인의 리뷰만 삭제할 수 있습니다."));
	}

	@Test
	void 리뷰수정_평점범위초과_예외() throws Exception {
		// 리뷰어 생성
		User reviewer = userRepository.save(User.builder().email(unique("reviewer") + "@test.com").password("pw")
				.nickname(unique("리뷰어")).role(UserType.USER).build());

		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(reviewer, null, reviewer.getAuthorities()));

		User reviewee = userRepository.save(User.builder().email(unique("reviewee") + "@test.com").password("pw")
				.nickname(unique("판매자")).role(UserType.USER).build());

		Product product = productRepository.save(Product.builder().title("상품").description("설명").price(10000)
				.category("기타").location("서울").status(ProductStatus.SOLD).user(reviewee).buyer(reviewer).build());

		Review review = reviewRepository.save(Review.builder().product(product).reviewer(reviewer).reviewee(reviewee)
				.rating(4).content("원래 내용").build());

		// 이미지 포함하지 않고, rating 초과로 제출
		mockMvc.perform(multipart("/api/reviews/{reviewId}", review.getId()).param("rating", "6")
				.param("content", "점수 초과").with(request -> {
					request.setMethod("PATCH");
					return request;
				}).contentType(MediaType.MULTIPART_FORM_DATA).characterEncoding("UTF-8"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void 리뷰작성_내용공백_예외() throws Exception {
		MockMultipartFile image = new MockMultipartFile("images", "img.jpg", MediaType.IMAGE_JPEG_VALUE,
				"test".getBytes());

		mockMvc.perform(multipart("/api/reviews").file(image).param("productId", product.getId().toString())
				.param("revieweeId", reviewee.getId().toString()).param("rating", "4").param("content", "   ") // 공백 문자열
				.contentType(MediaType.MULTIPART_FORM_DATA).characterEncoding("UTF-8"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void 리뷰상세조회_성공() throws Exception {
		// given
		Review review = reviewRepository.save(Review.builder().product(product).reviewer(buyer).reviewee(reviewee)
				.rating(5).content("상세 조회용 리뷰입니다.").build());

		// when & then
		mockMvc.perform(get("/api/reviews/{reviewId}", review.getId()).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.reviewId").value(review.getId()))
				.andExpect(jsonPath("$.rating").value(5)).andExpect(jsonPath("$.content").value("상세 조회용 리뷰입니다."))
				.andExpect(jsonPath("$.reviewerId").value(buyer.getId()))
				.andExpect(jsonPath("$.revieweeId").value(reviewee.getId()));
	}

}
