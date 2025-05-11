package com.example.moneytalk;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;

import com.amazonaws.services.s3.AmazonS3;
import com.example.moneytalk.config.S3Uploader;

@SpringBootTest
@ActiveProfiles("test")
class MoneytalkApplicationTests {
	
    @MockBean
    private AmazonS3 amazonS3;
    
    @MockBean
    private S3Uploader s3Uploader;
    
    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    
	@Test
	void contextLoads() {
	}

}
