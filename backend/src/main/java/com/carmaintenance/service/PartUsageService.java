package com.carmaintenance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.carmaintenance.entity.PartUsage;

import java.util.List;

public interface PartUsageService extends IService<PartUsage> {
    List<PartUsage> listByOrderId(Long orderId);
}
