package com.carmaintenance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carmaintenance.common.Result;
import com.carmaintenance.entity.Customer;
import com.carmaintenance.entity.RepairOrder;
import com.carmaintenance.entity.Vehicle;
import com.carmaintenance.mapper.PartMapper;
import com.carmaintenance.mapper.PartUsageMapper;
import com.carmaintenance.mapper.RepairOrderMapper;
import com.carmaintenance.service.CustomerService;
import com.carmaintenance.service.RepairOrderService;
import com.carmaintenance.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final CustomerService customerService;
    private final VehicleService vehicleService;
    private final RepairOrderService repairOrderService;
    private final RepairOrderMapper repairOrderMapper;
    private final PartMapper partMapper;
    private final PartUsageMapper partUsageMapper;

    @GetMapping
    public Result<?> getDashboard() {
        Map<String, Object> data = new HashMap<>();

        // 基础统计
        data.put("customerCount", customerService.count());
        data.put("vehicleCount", vehicleService.count());
        data.put("totalOrders", repairOrderService.count());

        // 待处理工单数
        data.put("pendingOrders", repairOrderService.count(
                new LambdaQueryWrapper<RepairOrder>().eq(RepairOrder::getStatus, 0)));
        data.put("inProgressOrders", repairOrderService.count(
                new LambdaQueryWrapper<RepairOrder>().eq(RepairOrder::getStatus, 1)));
        data.put("inspectingOrders", repairOrderService.count(
                new LambdaQueryWrapper<RepairOrder>().eq(RepairOrder::getStatus, 2)));
        data.put("completedOrders", repairOrderService.count(
                new LambdaQueryWrapper<RepairOrder>().eq(RepairOrder::getStatus, 3)));

        // 总营收
        LambdaQueryWrapper<RepairOrder> paidWrapper = new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getIsPaid, 1).eq(RepairOrder::getStatus, 3);
        data.put("totalRevenue", repairOrderService.list(paidWrapper).stream()
                .map(RepairOrder::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // 工单状态分布
        data.put("statusDistribution", repairOrderMapper.countByStatus());

        // 近6个月趋势
        String sixMonthsAgo = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        data.put("monthlyOrders", repairOrderMapper.countByMonth(sixMonthsAgo));
        data.put("monthlyRevenue", repairOrderMapper.revenueByMonth(sixMonthsAgo));

        // 库存预警
        data.put("lowStockParts", partMapper.findLowStockParts());

        // 常用配件 Top10
        data.put("topUsedParts", partUsageMapper.topUsedParts());

        return Result.success(data);
    }
}
