package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.carmaintenance.entity.ChatConversation;
import com.carmaintenance.entity.ChatMessage;
import com.carmaintenance.entity.SysUser;
import com.carmaintenance.mapper.ChatConversationMapper;
import com.carmaintenance.mapper.ChatMessageMapper;
import com.carmaintenance.mapper.SysUserMapper;
import com.carmaintenance.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    public static final String ROLE_USER = "USER";
    public static final String ROLE_CS = "CS";

    private final ChatConversationMapper conversationMapper;
    private final ChatMessageMapper messageMapper;
    private final SysUserMapper userMapper;

    @Override
    public ChatConversation getOrCreateConversation(Long userId) {
        ChatConversation conv = conversationMapper.selectOne(
                new LambdaQueryWrapper<ChatConversation>().eq(ChatConversation::getUserId, userId));
        if (conv != null) return conv;
        conv = new ChatConversation();
        conv.setUserId(userId);
        conv.setUserUnread(0);
        conv.setCsUnread(0);
        conversationMapper.insert(conv);
        return conv;
    }

    @Override
    public ChatConversation getOrCreateConversationById(Long conversationId) {
        return conversationMapper.selectById(conversationId);
    }

    @Override
    @Transactional
    public ChatMessage saveMessage(Long conversationId, String senderRole, Long senderId, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setConversationId(conversationId);
        msg.setSenderRole(senderRole);
        msg.setSenderId(senderId);
        msg.setContent(content);
        msg.setIsRead(0);
        messageMapper.insert(msg);

        ChatConversation conv = conversationMapper.selectById(conversationId);
        if (conv != null) {
            conv.setLastMessage(content.length() > 100 ? content.substring(0, 100) : content);
            conv.setLastMessageAt(LocalDateTime.now());
            if (ROLE_USER.equals(senderRole)) {
                conv.setCsUnread((conv.getCsUnread() == null ? 0 : conv.getCsUnread()) + 1);
            } else {
                conv.setUserUnread((conv.getUserUnread() == null ? 0 : conv.getUserUnread()) + 1);
            }
            conversationMapper.updateById(conv);
        }
        return msg;
    }

    @Override
    public List<ChatMessage> listMessages(Long conversationId) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getConversationId, conversationId)
                        .orderByAsc(ChatMessage::getId));
    }

    @Override
    public void markReadByUser(Long conversationId) {
        messageMapper.update(null, new LambdaUpdateWrapper<ChatMessage>()
                .eq(ChatMessage::getConversationId, conversationId)
                .eq(ChatMessage::getSenderRole, ROLE_CS)
                .eq(ChatMessage::getIsRead, 0)
                .set(ChatMessage::getIsRead, 1));
        conversationMapper.update(null, new LambdaUpdateWrapper<ChatConversation>()
                .eq(ChatConversation::getId, conversationId)
                .set(ChatConversation::getUserUnread, 0));
    }

    @Override
    public void markReadByCs(Long conversationId) {
        messageMapper.update(null, new LambdaUpdateWrapper<ChatMessage>()
                .eq(ChatMessage::getConversationId, conversationId)
                .eq(ChatMessage::getSenderRole, ROLE_USER)
                .eq(ChatMessage::getIsRead, 0)
                .set(ChatMessage::getIsRead, 1));
        conversationMapper.update(null, new LambdaUpdateWrapper<ChatConversation>()
                .eq(ChatConversation::getId, conversationId)
                .set(ChatConversation::getCsUnread, 0));
    }

    @Override
    public List<Map<String, Object>> listConversationsForCs() {
        List<ChatConversation> convs = conversationMapper.selectList(
                new LambdaQueryWrapper<ChatConversation>()
                        .orderByDesc(ChatConversation::getLastMessageAt));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ChatConversation c : convs) {
            SysUser u = userMapper.selectById(c.getUserId());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", c.getId());
            item.put("userId", c.getUserId());
            item.put("username", u != null ? u.getUsername() : null);
            item.put("realName", u != null ? u.getRealName() : null);
            item.put("avatar", u != null ? u.getAvatar() : null);
            item.put("lastMessage", c.getLastMessage());
            item.put("lastMessageAt", c.getLastMessageAt());
            item.put("unread", c.getCsUnread());
            result.add(item);
        }
        return result;
    }

    @Override
    public Map<String, Object> getConversationDetail(Long conversationId) {
        ChatConversation c = conversationMapper.selectById(conversationId);
        if (c == null) return null;
        SysUser u = userMapper.selectById(c.getUserId());
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", c.getId());
        item.put("userId", c.getUserId());
        item.put("username", u != null ? u.getUsername() : null);
        item.put("realName", u != null ? u.getRealName() : null);
        item.put("avatar", u != null ? u.getAvatar() : null);
        item.put("unread", c.getCsUnread());
        return item;
    }

    @Override
    public int getUserUnread(Long userId) {
        ChatConversation c = conversationMapper.selectOne(
                new LambdaQueryWrapper<ChatConversation>().eq(ChatConversation::getUserId, userId));
        return (c == null || c.getUserUnread() == null) ? 0 : c.getUserUnread();
    }

    @Override
    public int getCsTotalUnread() {
        List<ChatConversation> convs = conversationMapper.selectList(null);
        int total = 0;
        for (ChatConversation c : convs) {
            if (c.getCsUnread() != null) total += c.getCsUnread();
        }
        return total;
    }
}
