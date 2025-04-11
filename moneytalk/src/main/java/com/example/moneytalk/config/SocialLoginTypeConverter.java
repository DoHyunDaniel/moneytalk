package com.example.moneytalk.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import com.example.moneytalk.type.SocialLoginType;

@Configuration
public class SocialLoginTypeConverter implements Converter<String, SocialLoginType> {
    @Override
    public SocialLoginType convert(String s) {
        return SocialLoginType.valueOf(s.toUpperCase());
    }
}