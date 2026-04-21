package com.carmaintenance.websocket;

import com.carmaintenance.entity.SysRole;
import com.carmaintenance.entity.SysUser;
import com.carmaintenance.mapper.SysRoleMapper;
import com.carmaintenance.mapper.SysUserMapper;
import com.carmaintenance.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ChatHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest)) return false;
        String token = ((ServletServerHttpRequest) request).getServletRequest().getParameter("token");
        if (token == null || token.isBlank() || !jwtUtils.validateToken(token)) return false;
        try {
            Long userId = jwtUtils.getUserId(token);
            String roleKey = jwtUtils.getRoleKey(token);
            SysUser user = userMapper.selectById(userId);
            if (user == null || user.getStatus() == null || user.getStatus() != 1) return false;
            if (roleKey == null) {
                SysRole role = roleMapper.selectById(user.getRoleId());
                roleKey = role != null ? role.getRoleKey() : "";
            }
            attributes.put("userId", userId);
            attributes.put("roleKey", roleKey);
            attributes.put("username", user.getUsername());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
