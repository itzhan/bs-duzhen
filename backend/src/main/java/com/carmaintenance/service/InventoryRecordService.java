package com.carmaintenance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.carmaintenance.entity.InventoryRecord;

public interface InventoryRecordService extends IService<InventoryRecord> {
    IPage<InventoryRecord> pageList(int page, int size, Long partId, Integer type);
}
