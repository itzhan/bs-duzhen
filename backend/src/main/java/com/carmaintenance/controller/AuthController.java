package com.carmaintenance.controller;

import com.carmaintenance.common.Result;
import com.carmaintenance.dto.LoginDTO;
import com.carmaintenance.dto.PasswordDTO;
import com.carmaintenance.entity.SysRole;
import com.carmaintenance.entity.SysUser;
import com.carmaintenance.mapper.SysRoleMapper;
import com.carmaintenance.security.JwtUtils;
import com.carmaintenance.security.LoginUser;
import com.carmaintenance.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final SysUserService userService;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        SysUser user = loginUser.getUser();

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userService.updateById(user);

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), loginUser.getRoleKey());

        SysRole role = roleMapper.selectById(user.getRoleId());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("avatar", user.getAvatar());
        data.put("roleId", user.getRoleId());
        data.put("roleKey", loginUser.getRoleKey());
        data.put("roleName", role != null ? role.getRoleName() : "");

        return Result.success("登录成功", data);
    }

    @GetMapping("/info")
    public Result<?> getUserInfo() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SysUser user = loginUser.getUser();
        SysRole role = roleMapper.selectById(user.getRoleId());

        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("phone", user.getPhone());
        data.put("email", user.getEmail());
        data.put("avatar", user.getAvatar());
        data.put("roleId", user.getRoleId());
        data.put("roleKey", loginUser.getRoleKey());
        data.put("roleName", role != null ? role.getRoleName() : "");

        return Result.success(data);
    }

    @PutMapping("/password")
    public Result<?> changePassword(@Valid @RequestBody PasswordDTO dto) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SysUser user = loginUser.getUser();

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            return Result.badRequest("旧密码错误");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userService.updateById(user);
        return Result.success("密码修改成功");
    }
}
