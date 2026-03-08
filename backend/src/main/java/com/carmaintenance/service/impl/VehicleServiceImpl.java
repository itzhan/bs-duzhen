package com.carmaintenance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.carmaintenance.entity.Vehicle;
import com.carmaintenance.mapper.VehicleMapper;
import com.carmaintenance.service.VehicleService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class VehicleServiceImpl extends ServiceImpl<VehicleMapper, Vehicle> implements VehicleService {

    @Override
    public IPage<Vehicle> pageList(int page, int size, String keyword, Long customerId) {
        LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Vehicle::getPlateNumber, keyword)
                    .or().like(Vehicle::getBrand, keyword)
                    .or().like(Vehicle::getModel, keyword)
                    .or().like(Vehicle::getVin, keyword));
        }
        if (customerId != null) {
            wrapper.eq(Vehicle::getCustomerId, customerId);
        }
        wrapper.orderByDesc(Vehicle::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public List<Vehicle> listByCustomerId(Long customerId) {
        return list(new LambdaQueryWrapper<Vehicle>().eq(Vehicle::getCustomerId, customerId));
    }
}
