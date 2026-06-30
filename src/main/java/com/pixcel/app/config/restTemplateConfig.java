package com.pixcel.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class restTemplateConfig {
	
	@Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
