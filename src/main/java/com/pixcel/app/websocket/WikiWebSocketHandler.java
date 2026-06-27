package com.pixcel.app.websocket;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

public class WikiWebSocketHandler extends BinaryWebSocketHandler {

    // wiki_id 별로 세션 관리
    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String wikiId = getWikiId(session);
        rooms.computeIfAbsent(wikiId, k -> ConcurrentHashMap.newKeySet()).add(session);
        System.out.println("연결됨 - wikiId: " + wikiId + ", 세션: " + session.getId());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        String wikiId = getWikiId(session);
        Set<WebSocketSession> room = rooms.get(wikiId);

        if (room == null) return;

        // 같은 위키 페이지 사람들한테 broadcast
        for (WebSocketSession s : room) {
            if (s.isOpen() && !s.getId().equals(session.getId())) {
                s.sendMessage(new BinaryMessage(message.getPayload()));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String wikiId = getWikiId(session);
        Set<WebSocketSession> room = rooms.get(wikiId);

        if (room != null) {
            room.remove(session);
            if (room.isEmpty()) {
                rooms.remove(wikiId);
            }
        }
        System.out.println("연결종료 - wikiId: " + wikiId + ", 세션: " + session.getId());
    }

    private String getWikiId(WebSocketSession session) {
        String path = session.getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}