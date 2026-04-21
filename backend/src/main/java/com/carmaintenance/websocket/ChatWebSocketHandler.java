package com.carmaintenance.websocket;

import com.carmaintenance.entity.ChatConversation;
import com.carmaintenance.entity.ChatMessage;
import com.carmaintenance.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_CUSTOMER_SERVICE = "CUSTOMER_SERVICE";

    /** 顾客 userId -> session */
    private final Map<Long, WebSocketSession> customerSessions = new ConcurrentHashMap<>();
    /** 客服 userId -> session（支持多个客服同时在线） */
    private final Map<Long, WebSocketSession> csSessions = new ConcurrentHashMap<>();

    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        String roleKey = (String) session.getAttributes().get("roleKey");
        if (userId == null || roleKey == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        if (ROLE_CUSTOMER.equals(roleKey)) {
            customerSessions.put(userId, session);
            chatService.getOrCreateConversation(userId);
        } else if (ROLE_CUSTOMER_SERVICE.equals(roleKey) || "ADMIN".equals(roleKey)) {
            csSessions.put(userId, session);
        } else {
            session.close(CloseStatus.POLICY_VIOLATION);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        String roleKey = (String) session.getAttributes().get("roleKey");
        if (userId == null || roleKey == null) return;

        Map<String, Object> payload;
        try {
            payload = objectMapper.readValue(message.getPayload(), Map.class);
        } catch (Exception e) {
            return;
        }
        String type = String.valueOf(payload.getOrDefault("type", ""));

        switch (type) {
            case "PING":
                sendJson(session, Map.of("type", "PONG"));
                return;
            case "SEND":
                handleSend(session, userId, roleKey, payload);
                return;
            case "READ":
                handleRead(userId, roleKey, payload);
                return;
            default:
        }
    }

    private void handleSend(WebSocketSession session, Long userId, String roleKey, Map<String, Object> payload) throws Exception {
        String content = payload.get("content") == null ? "" : payload.get("content").toString().trim();
        if (content.isEmpty()) return;
        if (content.length() > 2000) content = content.substring(0, 2000);

        ChatConversation conv;
        String senderRole;
        if (ROLE_CUSTOMER.equals(roleKey)) {
            conv = chatService.getOrCreateConversation(userId);
            senderRole = "USER";
        } else {
            Object cidObj = payload.get("conversationId");
            if (cidObj == null) return;
            Long conversationId = Long.valueOf(cidObj.toString());
            conv = new ChatConversation();
            conv.setId(conversationId);
            // fetch to find target user
            ChatConversation detail = chatService.getOrCreateConversationById(conversationId);
            if (detail == null) return;
            conv = detail;
            senderRole = "CS";
        }

        ChatMessage saved = chatService.saveMessage(conv.getId(), senderRole, userId, content);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("type", "MESSAGE");
        out.put("message", saved);
        out.put("conversationUserId", conv.getUserId());

        if ("USER".equals(senderRole)) {
            // echo to sender (so their own UI shows confirmed id/time)
            sendJson(session, out);
            // push to all CS
            for (WebSocketSession cs : csSessions.values()) sendJson(cs, out);
            // push unread count update to CS
            pushCsUnread();
        } else {
            // echo to sending CS
            sendJson(session, out);
            // push to the specific user
            WebSocketSession us = customerSessions.get(conv.getUserId());
            if (us != null) sendJson(us, out);
            // update CS's own view (other CS sessions)
            for (Map.Entry<Long, WebSocketSession> e : csSessions.entrySet()) {
                if (!e.getKey().equals(userId)) sendJson(e.getValue(), out);
            }
            // push unread to specific user
            if (us != null) {
                sendJson(us, Map.of("type", "UNREAD", "count", chatService.getUserUnread(conv.getUserId())));
            }
        }
    }

    private void handleRead(Long userId, String roleKey, Map<String, Object> payload) throws Exception {
        if (ROLE_CUSTOMER.equals(roleKey)) {
            ChatConversation conv = chatService.getOrCreateConversation(userId);
            chatService.markReadByUser(conv.getId());
        } else {
            Object cidObj = payload.get("conversationId");
            if (cidObj == null) return;
            Long conversationId = Long.valueOf(cidObj.toString());
            chatService.markReadByCs(conversationId);
            pushCsUnread();
        }
    }

    private void pushCsUnread() throws Exception {
        int total = chatService.getCsTotalUnread();
        Map<String, Object> msg = Map.of("type", "UNREAD", "count", total);
        for (WebSocketSession cs : csSessions.values()) sendJson(cs, msg);
    }

    private void sendJson(WebSocketSession session, Object obj) {
        if (session == null || !session.isOpen()) return;
        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(obj)));
            }
        } catch (Exception e) {
            log.warn("WS send failed", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) return;
        customerSessions.remove(userId, session);
        csSessions.remove(userId, session);
    }
}
