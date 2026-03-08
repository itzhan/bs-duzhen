package com.carmaintenance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carmaintenance.common.BusinessException;
import com.carmaintenance.common.PageResult;
import com.carmaintenance.common.Result;
import com.carmaintenance.dto.SysUserDTO;
import com.carmaintenance.entity.SysRole;
import com.carmaintenance.entity.SysUser;
import com.carmaintenance.mapper.SysRoleMapper;
import com.carmaintenance.service.SysUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Long roleId) {
        return Result.success(PageResult.of(userService.pageList(page, size, keyword, roleId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> getById(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> create(@Valid @RequestBody SysUserDTO dto) {
        // 检查用户名是否重复
        long count = userService.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "123456"));
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        userService.save(user);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody SysUserDTO dto) {
        SysUser user = userService.getById(id);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        // 检查用户名是否重复
        long count = userService.count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername())
                .ne(SysUser::getId, id));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        BeanUtils.copyProperties(dto, user, "id", "password");
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        userService.updateById(user);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> resetPassword(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        userService.updateById(user);
        return Result.success("密码已重置为 123456");
    }

    @GetMapping("/roles")
    public Result<?> getRoles() {
        List<SysRole> roles = roleMapper.selectList(null);
        return Result.success(roles);
    }

    @GetMapping("/technicians")
    public Result<?> getTechnicians() {
        List<SysUser> technicians = userService.list(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getRoleId, 3)
                        .eq(SysUser::getStatus, 1));
        technicians.forEach(t -> t.setPassword(null));
        return Result.success(technicians);
    }

    @GetMapping("/advisors")
    public Result<?> getAdvisors() {
        List<SysUser> advisors = userService.list(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getRoleId, 2)
                        .eq(SysUser::getStatus, 1));
        advisors.forEach(a -> a.setPassword(null));
        return Result.success(advisors);
    }
}
