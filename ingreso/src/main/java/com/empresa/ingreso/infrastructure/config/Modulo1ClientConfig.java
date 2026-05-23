package com.empresa.ingreso.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class Modulo1ClientConfig {

    @Value("${modules.modulo1.base-url}")
    private String modulo1Url;

    @Bean
    public RestTemplate modulo1RestTemplate(RestTemplateBuilder builder) {
        return builder
                .rootUri(modulo1Url)
                .build();
    }
}
