package com.carmaintenance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.carmaintenance.entity.SysDict;
import com.carmaintenance.entity.SysDictItem;

import java.util.List;

public interface SysDictService extends IService<SysDict> {
    List<SysDictItem> getItemsByDictCode(String dictCode);
}
