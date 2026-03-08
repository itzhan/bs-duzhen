package com.carmaintenance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.carmaintenance.entity.Customer;

public interface CustomerService extends IService<Customer> {
    IPage<Customer> pageList(int page, int size, String keyword);
}
