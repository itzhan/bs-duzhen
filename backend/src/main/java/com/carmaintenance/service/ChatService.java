package com.carmaintenance.service;

import com.carmaintenance.entity.ChatConversation;
import com.carmaintenance.entity.ChatMessage;

import java.util.List;
import java.util.Map;

public interface ChatService {

    /** 获取或创建顾客的会话 */
    ChatConversation getOrCreateConversation(Long userId);

    /** 通过会话ID获取会话 */
    ChatConversation getOrCreateConversationById(Long conversationId);

    /** 保存消息并更新会话摘要/未读数 */
    ChatMessage saveMessage(Long conversationId, String senderRole, Long senderId, String content);

    /** 顾客 - 查看自己的会话历史 */
    List<ChatMessage> listMessages(Long conversationId);

    /** 顾客标记客服发来的消息为已读（清零 userUnread） */
    void markReadByUser(Long conversationId);

    /** 客服标记顾客发来的消息为已读（清零 csUnread） */
    void markReadByCs(Long conversationId);

    /** 客服 - 获取会话列表（包含用户名、未读数等） */
    List<Map<String, Object>> listConversationsForCs();

    /** 客服 - 获取指定会话（附带用户信息） */
    Map<String, Object> getConversationDetail(Long conversationId);

    /** 顾客 - 获取自己的未读数 */
    int getUserUnread(Long userId);

    /** 客服 - 获取总未读数 */
    int getCsTotalUnread();
}
