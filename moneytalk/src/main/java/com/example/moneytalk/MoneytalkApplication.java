package com.example.moneytalk;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.example.moneytalk.config.DotenvPropertyInitializer;

@SpringBootApplication
public class MoneytalkApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(MoneytalkApplication.class)
                .initializers(new DotenvPropertyInitializer())
                .run(args);
    }
}
