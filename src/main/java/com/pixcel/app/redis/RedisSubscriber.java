//package com.pixcel.app.redis;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.connection.Message;
//import org.springframework.data.redis.connection.MessageListener;
//import org.springframework.stereotype.Component;
//
//import com.pixcel.app.websocket.WikiWebSocketHandler;
//
//@Component
//public class RedisSubscriber implements MessageListener {
//
//    @Autowired
//    private WikiWebSocketHandler handler;
//
//    @Override
//    public void onMessage(Message message, byte[] pattern) {
//
//        String channel = new String(message.getChannel());
//        byte[] body = message.getBody();
//
//        System.out.println("Subscribe : " + channel);
//
//        handler.broadcast(channel, body);
//    }
//}