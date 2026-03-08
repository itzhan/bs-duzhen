package com.carmaintenance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.carmaintenance.entity.ServiceReminder;

public interface ServiceReminderService extends IService<ServiceReminder> {
    IPage<ServiceReminder> pageList(int page, int size, Integer type, Integer status, Long customerId);
}
