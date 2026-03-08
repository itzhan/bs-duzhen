package com.carmaintenance.controller;

import com.carmaintenance.common.PageResult;
import com.carmaintenance.common.Result;
import com.carmaintenance.dto.RepairOrderDTO;
import com.carmaintenance.entity.Customer;
import com.carmaintenance.entity.RepairOrder;
import com.carmaintenance.entity.SysUser;
import com.carmaintenance.entity.Vehicle;
import com.carmaintenance.security.LoginUser;
import com.carmaintenance.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;

@RestController
@RequestMapping("/api/repair-orders")
@RequiredArgsConstructor
public class RepairOrderController {

    private final RepairOrderService repairOrderService;
    private final RepairItemService repairItemService;
    private final PartUsageService partUsageService;
    private final CustomerService customerService;
    private final VehicleService vehicleService;
    private final SysUserService sysUserService;

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Integer status,
                          @RequestParam(required = false) Long customerId,
                          @RequestParam(required = false) Long technicianId) {
        IPage<RepairOrder> pageResult = repairOrderService.pageList(page, size, keyword, status, customerId, technicianId);
        // 补充关联名称
        List<Map<String, Object>> enrichedRecords = pageResult.getRecords().stream().map(order -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", order.getId());
            map.put("orderNo", order.getOrderNo());
            map.put("customerId", order.getCustomerId());
            map.put("vehicleId", order.getVehicleId());
            map.put("technicianId", order.getTechnicianId());
            map.put("status", order.getStatus());
            map.put("faultDesc", order.getFaultDesc());
            map.put("diagnosis", order.getDiagnosis());
            map.put("intakeMileage", order.getIntakeMileage());
            map.put("estimatedFinishTime", order.getEstimatedFinishTime());
            map.put("actualFinishTime", order.getActualFinishTime());
            map.put("laborCost", order.getLaborCost());
            map.put("partsCost", order.getPartsCost());
            map.put("totalCost", order.getTotalCost());
            map.put("isPaid", order.getIsPaid());
            map.put("remark", order.getRemark());
            map.put("createdAt", order.getCreatedAt());
            map.put("updatedAt", order.getUpdatedAt());

            if (order.getCustomerId() != null) {
                Customer c = customerService.getById(order.getCustomerId());
                map.put("customerName", c != null ? c.getName() : "");
            }
            if (order.getVehicleId() != null) {
                Vehicle v = vehicleService.getById(order.getVehicleId());
                map.put("plateNumber", v != null ? v.getPlateNumber() : "");
            }
            if (order.getTechnicianId() != null) {
                SysUser u = sysUserService.getById(order.getTechnicianId());
                map.put("technicianName", u != null ? u.getRealName() : "");
            }
            return map;
        }).collect(java.util.stream.Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", enrichedRecords);
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("size", pageResult.getSize());
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        RepairOrder order = repairOrderService.getById(id);
        if (order == null) {
            return Result.notFound("工单不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("items", repairItemService.listByOrderId(id));
        data.put("partUsages", partUsageService.listByOrderId(id));

        // 补充关联名称
        if (order.getCustomerId() != null) {
            Customer c = customerService.getById(order.getCustomerId());
            data.put("customerName", c != null ? c.getName() : "");
        }
        if (order.getVehicleId() != null) {
            Vehicle v = vehicleService.getById(order.getVehicleId());
            data.put("plateNumber", v != null ? v.getPlateNumber() : "");
        }
        if (order.getTechnicianId() != null) {
            SysUser u = sysUserService.getById(order.getTechnicianId());
            data.put("technicianName", u != null ? u.getRealName() : "");
        }

        return Result.success(data);
    }

    @PostMapping
    public Result<?> create(@Valid @RequestBody RepairOrderDTO dto) {
        RepairOrder order = new RepairOrder();
        BeanUtils.copyProperties(dto, order);
        order.setOrderNo(repairOrderService.generateOrderNo());
        order.setStatus(0);
        order.setIsPaid(0);

        repairOrderService.save(order);
        return Result.success("工单创建成功", order);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody RepairOrderDTO dto) {
        RepairOrder order = repairOrderService.getById(id);
        if (order == null) {
            return Result.notFound("工单不存在");
        }
        BeanUtils.copyProperties(dto, order, "id", "orderNo", "status", "isPaid");
        repairOrderService.updateById(order);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        repairOrderService.removeById(id);
        return Result.success("删除成功");
    }

    @PutMapping("/{id}/assign")
    public Result<?> assignTechnician(@PathVariable Long id, @RequestParam Long technicianId) {
        repairOrderService.assignTechnician(id, technicianId);
        return Result.success("派工成功");
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        repairOrderService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }

    @PutMapping("/{id}/settle")
    public Result<?> settle(@PathVariable Long id) {
        repairOrderService.settle(id);
        return Result.success("结算成功");
    }

    @GetMapping("/statistics")
    public Result<?> getStatistics() {
        return Result.success(repairOrderService.getStatistics());
    }
}
