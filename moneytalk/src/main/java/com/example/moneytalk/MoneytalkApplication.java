package com.example.moneytalk;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class MoneytalkApplication {
	public static void main(String[] args) {
		new SpringApplicationBuilder(MoneytalkApplication.class).run(args);
	}
}
