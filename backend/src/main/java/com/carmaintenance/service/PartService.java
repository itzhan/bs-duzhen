package com.carmaintenance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.carmaintenance.entity.Part;

import java.util.List;
import java.util.Map;

public interface PartService extends IService<Part> {
    IPage<Part> pageList(int page, int size, String keyword, String category);
    List<Map<String, Object>> getLowStockParts();
    void adjustStock(Long partId, int quantity, int type, Long operatorId, Long orderId, String remark);
}
