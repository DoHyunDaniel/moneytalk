package com.example.moneytalk.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;
import java.util.stream.Collectors;

public class DotenvPropertyInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Dotenv dotenv = Dotenv.load();

        // 정확히 Map<String, Object>로 타입 명시
        Map<String, Object> envMap = dotenv.entries().stream()
                .collect(Collectors.toMap(
                        io.github.cdimascio.dotenv.DotenvEntry::getKey,
                        entry -> (Object) entry.getValue()
                ));

        MapPropertySource propertySource = new MapPropertySource("dotenv", envMap);
        applicationContext.getEnvironment().getPropertySources().addFirst(propertySource);
    }
}
