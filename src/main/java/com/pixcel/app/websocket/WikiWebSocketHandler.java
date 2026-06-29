//package com.pixcel.app.websocket;
//
//
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.BinaryMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.BinaryWebSocketHandler;
//
//import com.pixcel.app.redis.RedisPublisher;
//
//@Component
//public class WikiWebSocketHandler extends BinaryWebSocketHandler {
//
//    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//
//        String wikiId = getWikiId(session);
//
//        session.getAttributes().put("wikiId", wikiId);
//
//        rooms.computeIfAbsent(wikiId, k -> ConcurrentHashMap.newKeySet())
//             .add(session);
//
//        System.out.println("접속 : " + wikiId);
//    }
//
//    @Override
//    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
//
//        String wikiId = (String) session.getAttributes().get("wikiId");
//
//        byte[] payload = message.getPayload().array();
//
//        System.out.println("메시지 수신 : " + wikiId);
//
//        // 👉 Redis publish
//        redisPublisher.publish("wiki-room:" + wikiId, payload);
//    }
//
//    public void broadcast(String channel, byte[] message) {
//
//        String wikiId = channel.replace("wiki-room:", "");
//
//        Set<WebSocketSession> sessions = rooms.get(wikiId);
//        if (sessions == null) return;
//
//        for (WebSocketSession s : sessions) {
//            try {
//                if (s.isOpen()) {
//                    s.sendMessage(new BinaryMessage(message));
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private String getWikiId(WebSocketSession session) {
//        String query = session.getUri().getQuery();
//        return query.split("=")[1]; // ?wikiId=123
//    }
//
//    @Autowired
//    private RedisPublisher redisPublisher;
//}