package com.example.moneytalk.Integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.services.s3.AmazonS3;
import com.example.moneytalk.config.S3Uploader;
import com.example.moneytalk.domain.Product;
import com.example.moneytalk.domain.User;
import com.example.moneytalk.repository.LedgerRepository;
import com.example.moneytalk.repository.ProductRepository;
import com.example.moneytalk.repository.PurchaseHistoryRepository;
import com.example.moneytalk.repository.PurchaseRecordRepository;
import com.example.moneytalk.repository.UserRepository;
import com.example.moneytalk.service.ProductService;
import com.example.moneytalk.type.ProductStatus;
import com.example.moneytalk.type.PurchaseType;

@SpringBootTest
@Transactional
@ActiveProfiles("test") 
class ProductServiceIntegrationTest {

    @MockBean
    private AmazonS3 amazonS3;
    
    @MockBean
    private S3Uploader s3Uploader;
    
    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PurchaseRecordRepository purchaseRecordRepository;

    @Autowired
    private PurchaseHistoryRepository purchaseHistoryRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    @DisplayName("상품 구매 시 상태 변경 및 기록 생성 확인")
    @Test
    void confirmPurchase_정상동작_통합테스트() {
        // given
        User seller = userRepository.save(User.builder()
                .email("seller@example.com")
                .password("1234")
                .nickname("판매자")
                .build());

        User buyer = userRepository.save(User.builder()
                .email("buyer@example.com")
                .password("1234")
                .nickname("구매자")
                .build());

        Product product = productRepository.save(Product.builder()
                .user(seller)
                .title("의자")
                .description("튼튼해요")
                .price(50000)
                .category("가구")
                .location("서울")
                .status(ProductStatus.SALE)
                .build());

        // when
        productService.confirmPurchase(product.getId(), buyer);

        // then
        Product updated = productRepository.findById(product.getId()).get();

        assertThat(updated.getStatus()).isEqualTo(ProductStatus.SOLD);
        assertThat(updated.getBuyer().getId()).isEqualTo(buyer.getId());

        assertThat(purchaseRecordRepository.findByBuyer(buyer)).hasSize(1);
        assertThat(purchaseHistoryRepository.findByUserAndType(buyer, PurchaseType.PURCHASE)).hasSize(1);
        assertThat(purchaseHistoryRepository.findByUserAndType(seller, PurchaseType.SALE)).hasSize(1);

        assertThat(ledgerRepository.findByUserAndDateBetween(buyer,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1))).hasSize(1);

        assertThat(ledgerRepository.findByUserAndDateBetween(seller,
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(1))).hasSize(1);
    }
}
