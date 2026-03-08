package com.carmaintenance.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.carmaintenance.entity.Vehicle;

import java.util.List;

public interface VehicleService extends IService<Vehicle> {
    IPage<Vehicle> pageList(int page, int size, String keyword, Long customerId);
    List<Vehicle> listByCustomerId(Long customerId);
}
