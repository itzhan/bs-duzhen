package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carmaintenance.entity.ServiceReminder;
import com.carmaintenance.mapper.ServiceReminderMapper;
import com.carmaintenance.service.ServiceReminderService;
import org.springframework.stereotype.Service;

@Service
public class ServiceReminderServiceImpl extends ServiceImpl<ServiceReminderMapper, ServiceReminder> implements ServiceReminderService {

    @Override
    public IPage<ServiceReminder> pageList(int page, int size, Integer type, Integer status, Long customerId) {
        LambdaQueryWrapper<ServiceReminder> wrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            wrapper.eq(ServiceReminder::getType, type);
        }
        if (status != null) {
            wrapper.eq(ServiceReminder::getStatus, status);
        }
        if (customerId != null) {
            wrapper.eq(ServiceReminder::getCustomerId, customerId);
        }
        wrapper.orderByAsc(ServiceReminder::getRemindDate);
        return page(new Page<>(page, size), wrapper);
    }
}
