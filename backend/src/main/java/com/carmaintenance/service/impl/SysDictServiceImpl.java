package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carmaintenance.entity.SysDict;
import com.carmaintenance.entity.SysDictItem;
import com.carmaintenance.mapper.SysDictItemMapper;
import com.carmaintenance.mapper.SysDictMapper;
import com.carmaintenance.service.SysDictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements SysDictService {

    private final SysDictItemMapper dictItemMapper;

    @Override
    public List<SysDictItem> getItemsByDictCode(String dictCode) {
        SysDict dict = getOne(new LambdaQueryWrapper<SysDict>().eq(SysDict::getDictCode, dictCode));
        if (dict == null) {
            return Collections.emptyList();
        }
        return dictItemMapper.selectList(
                new LambdaQueryWrapper<SysDictItem>()
                        .eq(SysDictItem::getDictId, dict.getId())
                        .eq(SysDictItem::getStatus, 1)
                        .orderByAsc(SysDictItem::getSortOrder));
    }
}
