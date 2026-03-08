package com.carmaintenance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.carmaintenance.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    IPage<SysUser> pageList(int page, int size, String keyword, Long roleId);
}
