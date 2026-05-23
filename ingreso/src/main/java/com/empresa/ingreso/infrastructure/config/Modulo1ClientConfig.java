package com.empresa.ingreso.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class Modulo1ClientConfig {

    @Value("${modules.modulo1.base-url}")
    private String modulo1Url;

    @Bean
    public WebClient modulo1WebClient() {
        return WebClient.builder()
                .baseUrl(modulo1Url)
                .build();
    }
}
