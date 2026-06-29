package com.pixcel.app.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WikiWebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(wikiWebSocketHandler(), "/ws/wiki/{wikiId}")
                .setAllowedOrigins("*");
    }

    @Bean
    public WikiWebSocketHandler wikiWebSocketHandler() {
        return new WikiWebSocketHandler();
    }
}

