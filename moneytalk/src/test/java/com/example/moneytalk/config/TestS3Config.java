package com.example.moneytalk.config;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestS3Config {

    @Bean
    public AmazonS3 amazonS3() {
        return mock(AmazonS3.class);
    }
}
