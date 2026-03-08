package com.carmaintenance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.carmaintenance.entity.RepairOrder;

import java.util.Map;

public interface RepairOrderService extends IService<RepairOrder> {
    IPage<RepairOrder> pageList(int page, int size, String keyword, Integer status, Long customerId, Long technicianId);
    String generateOrderNo();
    void assignTechnician(Long orderId, Long technicianId);
    void updateStatus(Long orderId, Integer status);
    void settle(Long orderId);
    Map<String, Object> getStatistics();
}
