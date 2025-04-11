package com.example.moneytalk.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.moneytalk.domain.User;
import com.example.moneytalk.dto.ProductRequestDto;
import com.example.moneytalk.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Void> createProduct(
            @RequestBody @Valid ProductRequestDto requestDto,
            @AuthenticationPrincipal User user
    ) {
        productService.createProduct(requestDto, user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
