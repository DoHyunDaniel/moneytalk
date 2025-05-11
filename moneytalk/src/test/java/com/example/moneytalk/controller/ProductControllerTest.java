package com.example.moneytalk.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.amazonaws.services.s3.AmazonS3;
import com.example.moneytalk.config.S3Uploader;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.ProductRequestDto;
import com.example.moneytalk.dto.ProductResponseDto;
import com.example.moneytalk.exception.GlobalException;
import com.example.moneytalk.service.ProductService;
import com.example.moneytalk.type.ErrorCode;
import com.example.moneytalk.type.ProductStatus;
import com.example.moneytalk.type.UserType;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // 시큐리티 필터 제거 (필요 시 적용)
@ActiveProfiles("test")
class ProductControllerTest {

	@MockBean private ClientRegistrationRepository clientRegistrationRepository;
	
	@MockBean private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
	
    @MockBean
    private AmazonS3 amazonS3;
    
    @MockBean
    private S3Uploader s3Uploader;
	
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private ProductService productService;

	@Test
	void getProductById_정상조회() throws Exception {
		// given
		Long productId = 1L;
		ProductResponseDto responseDto = ProductResponseDto.builder().id(productId).title("아이폰 14")
				.description("미개봉 새제품").price(1300000).category("전자기기").location("서울").status(ProductStatus.SALE)
				.createdAt(LocalDateTime.now()).sellerId(10L).sellerNickname("홍길동")
				.images(List.of("https://s3.aws.com/img1.jpg", "https://s3.aws.com/img2.jpg")).build();

		given(productService.getProductById(productId)).willReturn(responseDto);

		// when + then
		mockMvc.perform(get("/api/products/{id}", productId)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(productId)).andExpect(jsonPath("$.title").value("아이폰 14"))
				.andExpect(jsonPath("$.price").value(1300000)).andExpect(jsonPath("$.sellerNickname").value("홍길동"))
				.andExpect(jsonPath("$.images").isArray());
	}

	@Test
	void getProductById_상품없음_예외() throws Exception {
		// given
		Long productId = 999L;
		given(productService.getProductById(productId)).willThrow(new GlobalException(ErrorCode.PRODUCT_NOT_FOUND));

		// when + then
		mockMvc.perform(get("/api/products/{id}", productId)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("존재하지 않는 상품입니다.")); // GlobalExceptionHandler 기준
	}

	@Test
	void createProductWithImages_정상요청() throws Exception {
		// given
		ProductRequestDto requestDto = new ProductRequestDto();
		requestDto.setTitle("아이폰 14");
		requestDto.setDescription("미개봉");
		requestDto.setPrice(1200000);
		requestDto.setCategory("전자기기");
		requestDto.setLocation("서울");

		MockMultipartFile image1 = new MockMultipartFile("images", "image1.jpg", MediaType.IMAGE_JPEG_VALUE,
				"dummy-image-1".getBytes());

		MockMultipartFile image2 = new MockMultipartFile("images", "image2.jpg", MediaType.IMAGE_JPEG_VALUE,
				"dummy-image-2".getBytes());

		MockMultipartFile jsonPart = new MockMultipartFile("request", "", "application/json", """
				{
				  "title": "아이폰 14",
				  "description": "미개봉",
				  "price": 1200000,
				  "category": "전자기기",
				  "location": "서울"
				}
				""".getBytes());

		// when + then
		mockMvc.perform(multipart("/api/products").file(image1).file(image2).param("title", requestDto.getTitle())
				.param("description", requestDto.getDescription()).param("price", String.valueOf(requestDto.getPrice()))
				.param("category", requestDto.getCategory()).param("location", requestDto.getLocation())
				.contentType(MediaType.MULTIPART_FORM_DATA)).andExpect(status().isOk());

		// verify
		verify(productService).createProductWithImages(any(), any(), any());
	}

	@Test
	void updateProductStatus_정상요청() throws Exception {
		Long productId = 1L;

		User mockUser = User.builder().id(1L).nickname("tester").email("test@ex.com").password("encodedPassword")
				.role(UserType.USER).build();

		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities()));

		doNothing().when(productService).updateProductStatus(eq(productId), eq(ProductStatus.RESERVED), any());
		try {
			mockMvc.perform(
					patch("/api/products/{id}/status", productId).contentType(MediaType.APPLICATION_JSON).content("""
							    {
							        "status": "RESERVED"
							    }
							""")).andExpect(status().isNoContent());

			verify(productService).updateProductStatus(eq(productId), eq(ProductStatus.RESERVED), any());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void confirmPurchase_이미판매됨_예외() throws Exception {
		Long productId = 2L;

		doThrow(new GlobalException(ErrorCode.PRODUCT_ALREADY_SOLD)).when(productService).confirmPurchase(eq(productId),
				any());

		mockMvc.perform(patch("/api/products/{productId}/confirm", productId)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("이미 판매완료된 상품입니다."));
	}

}