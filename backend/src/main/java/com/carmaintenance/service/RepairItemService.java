package com.carmaintenance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.carmaintenance.entity.RepairItem;

import java.util.List;

public interface RepairItemService extends IService<RepairItem> {
    List<RepairItem> listByOrderId(Long orderId);
}
