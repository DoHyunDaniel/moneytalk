package com.example.moneytalk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import io.github.cdimascio.dotenv.Dotenv;

@Configuration
public class S3Config {

    private final Dotenv dotenv = Dotenv.load();

    @Bean
    public AmazonS3 amazonS3() {
        String accessKey = dotenv.get("AWS_ACCESS_KEY");
        String secretKey = dotenv.get("AWS_SECRET_KEY");
        String region = "us-east-1";

        if (accessKey == null || secretKey == null) {
            throw new IllegalArgumentException("Access key or secret key is missing!");
        }

        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }
}
