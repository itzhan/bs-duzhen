package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carmaintenance.entity.RepairItem;
import com.carmaintenance.mapper.RepairItemMapper;
import com.carmaintenance.service.RepairItemService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepairItemServiceImpl extends ServiceImpl<RepairItemMapper, RepairItem> implements RepairItemService {

    @Override
    public List<RepairItem> listByOrderId(Long orderId) {
        return list(new LambdaQueryWrapper<RepairItem>().eq(RepairItem::getOrderId, orderId));
    }
}
