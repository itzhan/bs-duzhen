package com.carmaintenance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carmaintenance.common.Result;
import com.carmaintenance.entity.*;
import com.carmaintenance.security.LoginUser;
import com.carmaintenance.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/technician-portal")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TECHNICIAN')")
public class TechnicianPortalController {

    private final RepairOrderService repairOrderService;
    private final RepairItemService repairItemService;
    private final PartUsageService partUsageService;
    private final ServiceReminderService reminderService;
    private final CustomerService customerService;
    private final VehicleService vehicleService;

    private Long getCurrentUserId() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return loginUser.getUser().getId();
    }

    /** 获取待接单工单列表 */
    @GetMapping("/available-orders")
    public Result<?> getAvailableOrders() {
        List<RepairOrder> orders = repairOrderService.list(
            new LambdaQueryWrapper<RepairOrder>()
                .eq(RepairOrder::getStatus, 0)
                .isNull(RepairOrder::getTechnicianId)
                .orderByDesc(RepairOrder::getCreatedAt));
        return Result.success(orders);
    }

    /** 接单 */
    @PutMapping("/orders/{id}/accept")
    public Result<?> acceptOrder(@PathVariable Long id) {
        RepairOrder order = repairOrderService.getById(id);
        if (order == null) return Result.notFound("工单不存在");
        if (order.getStatus() != 0) return Result.badRequest("只能接取待接单的工单");
        if (order.getTechnicianId() != null) return Result.badRequest("该工单已被其他技师接取");

        order.setTechnicianId(getCurrentUserId());
        order.setStatus(1);
        repairOrderService.updateById(order);
        return Result.success("接单成功");
    }

    /** 获取我的工单列表 */
    @GetMapping("/my-orders")
    public Result<?> getMyOrders(@RequestParam(required = false) Integer status) {
        Long userId = getCurrentUserId();
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<RepairOrder>()
            .eq(RepairOrder::getTechnicianId, userId);
        if (status != null) wrapper.eq(RepairOrder::getStatus, status);
        wrapper.orderByDesc(RepairOrder::getCreatedAt);
        return Result.success(repairOrderService.list(wrapper));
    }

    /** 获取工单详情 */
    @GetMapping("/orders/{id}")
    public Result<?> getOrderDetail(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        RepairOrder order = repairOrderService.getById(id);
        if (order == null) return Result.notFound("工单不存在");
        // 技师只能查看自己接的工单或待接单的工单
        if (order.getTechnicianId() != null && !order.getTechnicianId().equals(userId) && order.getStatus() != 0) {
            return Result.forbidden("无权查看此工单");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("items", repairItemService.listByOrderId(id));
        data.put("partUsages", partUsageService.listByOrderId(id));

        // 补充客户和车辆信息
        Customer customer = customerService.getById(order.getCustomerId());
        Vehicle vehicle = vehicleService.getById(order.getVehicleId());
        data.put("customerName", customer != null ? customer.getName() : "");
        data.put("customerPhone", customer != null ? customer.getPhone() : "");
        data.put("plateNumber", vehicle != null ? vehicle.getPlateNumber() : "");
        data.put("vehicleInfo", vehicle != null ? vehicle.getBrand() + " " + vehicle.getModel() : "");

        return Result.success(data);
    }

    /** 更新工单状态 */
    @PutMapping("/orders/{id}/status")
    public Result<?> updateOrderStatus(@PathVariable Long id, @RequestParam Integer status) {
        Long userId = getCurrentUserId();
        RepairOrder order = repairOrderService.getById(id);
        if (order == null) return Result.notFound("工单不存在");
        if (!userId.equals(order.getTechnicianId())) return Result.forbidden("只能更新自己负责的工单");

        repairOrderService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }

    /** 获取我的提醒列表 */
    @GetMapping("/reminders")
    public Result<?> getMyReminders() {
        Long userId = getCurrentUserId();
        List<ServiceReminder> reminders = reminderService.list(
            new LambdaQueryWrapper<ServiceReminder>()
                .eq(ServiceReminder::getTechnicianId, userId)
                .orderByDesc(ServiceReminder::getRemindDate));
        return Result.success(reminders);
    }

    /** 获取首页统计 */
    @GetMapping("/dashboard")
    public Result<?> getDashboard() {
        Long userId = getCurrentUserId();
        Map<String, Object> data = new HashMap<>();
        data.put("availableOrders", repairOrderService.count(
            new LambdaQueryWrapper<RepairOrder>().eq(RepairOrder::getStatus, 0).isNull(RepairOrder::getTechnicianId)));
        data.put("myActiveOrders", repairOrderService.count(
            new LambdaQueryWrapper<RepairOrder>().eq(RepairOrder::getTechnicianId, userId)
                .in(RepairOrder::getStatus, 1, 2)));
        data.put("myCompletedOrders", repairOrderService.count(
            new LambdaQueryWrapper<RepairOrder>().eq(RepairOrder::getTechnicianId, userId)
                .eq(RepairOrder::getStatus, 3)));
        data.put("pendingReminders", reminderService.count(
            new LambdaQueryWrapper<ServiceReminder>().eq(ServiceReminder::getTechnicianId, userId)
                .eq(ServiceReminder::getStatus, 0)));
        return Result.success(data);
    }
}
