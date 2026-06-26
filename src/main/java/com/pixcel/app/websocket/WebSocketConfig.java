//package com.pixcel.app.websocket;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.config.annotation.EnableWebSocket;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
//
//@Configuration
//@EnableWebSocket
//public class WebSocketConfig implements WebSocketConfigurer {
//
//    private final WikiWebSocketHandler wikiWebSocketHandler;
//
//    public WebSocketConfig(WikiWebSocketHandler wikiWebSocketHandler) {
//        this.wikiWebSocketHandler = wikiWebSocketHandler;
//    }
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//
//        registry.addHandler(wikiWebSocketHandler, "/ws/wiki")
//                .setAllowedOriginPatterns("*");
//    }
//}