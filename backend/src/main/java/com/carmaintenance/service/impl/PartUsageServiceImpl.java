package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carmaintenance.entity.PartUsage;
import com.carmaintenance.mapper.PartUsageMapper;
import com.carmaintenance.service.PartUsageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartUsageServiceImpl extends ServiceImpl<PartUsageMapper, PartUsage> implements PartUsageService {

    @Override
    public List<PartUsage> listByOrderId(Long orderId) {
        return list(new LambdaQueryWrapper<PartUsage>().eq(PartUsage::getOrderId, orderId));
    }
}
