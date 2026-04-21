package com.carmaintenance.controller;

import com.carmaintenance.common.Result;
import com.carmaintenance.entity.ChatConversation;
import com.carmaintenance.entity.ChatMessage;
import com.carmaintenance.security.LoginUser;
import com.carmaintenance.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    private LoginUser currentUser() {
        return (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // ============ 顾客端 ============

    /** 顾客 - 获取或创建自己的会话 */
    @GetMapping("/my-conversation")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<?> myConversation() {
        ChatConversation conv = chatService.getOrCreateConversation(currentUser().getUser().getId());
        return Result.success(conv);
    }

    /** 顾客 - 获取自己的历史消息 */
    @GetMapping("/my-messages")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<?> myMessages() {
        ChatConversation conv = chatService.getOrCreateConversation(currentUser().getUser().getId());
        List<ChatMessage> list = chatService.listMessages(conv.getId());
        return Result.success(list);
    }

    /** 顾客 - 标记全部已读 */
    @PostMapping("/my-read")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<?> myRead() {
        ChatConversation conv = chatService.getOrCreateConversation(currentUser().getUser().getId());
        chatService.markReadByUser(conv.getId());
        return Result.success();
    }

    /** 顾客 - 我的未读数 */
    @GetMapping("/my-unread")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<?> myUnread() {
        int n = chatService.getUserUnread(currentUser().getUser().getId());
        return Result.success(n);
    }

    // ============ 客服端 ============

    /** 客服 - 会话列表 */
    @GetMapping("/cs/conversations")
    @PreAuthorize("hasRole('CUSTOMER_SERVICE') or hasRole('ADMIN')")
    public Result<?> csConversations() {
        List<Map<String, Object>> list = chatService.listConversationsForCs();
        return Result.success(list);
    }

    /** 客服 - 指定会话消息 */
    @GetMapping("/cs/conversations/{id}/messages")
    @PreAuthorize("hasRole('CUSTOMER_SERVICE') or hasRole('ADMIN')")
    public Result<?> csMessages(@PathVariable("id") Long id) {
        return Result.success(chatService.listMessages(id));
    }

    /** 客服 - 会话详情（含用户信息） */
    @GetMapping("/cs/conversations/{id}")
    @PreAuthorize("hasRole('CUSTOMER_SERVICE') or hasRole('ADMIN')")
    public Result<?> csConversationDetail(@PathVariable("id") Long id) {
        return Result.success(chatService.getConversationDetail(id));
    }

    /** 客服 - 标记指定会话已读 */
    @PostMapping("/cs/conversations/{id}/read")
    @PreAuthorize("hasRole('CUSTOMER_SERVICE') or hasRole('ADMIN')")
    public Result<?> csRead(@PathVariable("id") Long id) {
        chatService.markReadByCs(id);
        return Result.success();
    }

    /** 客服 - 总未读数 */
    @GetMapping("/cs/unread")
    @PreAuthorize("hasRole('CUSTOMER_SERVICE') or hasRole('ADMIN')")
    public Result<?> csUnread() {
        return Result.success(chatService.getCsTotalUnread());
    }
}
