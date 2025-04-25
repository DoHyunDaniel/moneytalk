package com.example.moneytalk.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NicknameSuggestionResponseDto {
    private String base;
    private boolean available;
    private List<String> suggestions;
}
