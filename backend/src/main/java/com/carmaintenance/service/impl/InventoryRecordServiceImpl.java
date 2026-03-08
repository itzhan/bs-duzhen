package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carmaintenance.entity.InventoryRecord;
import com.carmaintenance.mapper.InventoryRecordMapper;
import com.carmaintenance.service.InventoryRecordService;
import org.springframework.stereotype.Service;

@Service
public class InventoryRecordServiceImpl extends ServiceImpl<InventoryRecordMapper, InventoryRecord> implements InventoryRecordService {

    @Override
    public IPage<InventoryRecord> pageList(int page, int size, Long partId, Integer type) {
        LambdaQueryWrapper<InventoryRecord> wrapper = new LambdaQueryWrapper<>();
        if (partId != null) {
            wrapper.eq(InventoryRecord::getPartId, partId);
        }
        if (type != null) {
            wrapper.eq(InventoryRecord::getType, type);
        }
        wrapper.orderByDesc(InventoryRecord::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }
}
