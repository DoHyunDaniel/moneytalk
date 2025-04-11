package com.example.moneytalk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequestDto {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Integer price;

    @NotBlank
    private String category;

    @NotBlank
    private String location;
}
