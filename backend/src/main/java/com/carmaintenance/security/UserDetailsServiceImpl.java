package com.carmaintenance.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carmaintenance.entity.SysRole;
import com.carmaintenance.entity.SysUser;
import com.carmaintenance.mapper.SysRoleMapper;
import com.carmaintenance.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        SysRole role = roleMapper.selectById(user.getRoleId());
        String roleKey = role != null ? role.getRoleKey() : "USER";
        return new LoginUser(user, roleKey);
    }
}
