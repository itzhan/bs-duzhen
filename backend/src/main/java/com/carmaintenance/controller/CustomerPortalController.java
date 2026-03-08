package com.carmaintenance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carmaintenance.common.Result;
import com.carmaintenance.dto.PaymentDTO;
import com.carmaintenance.entity.*;
import com.carmaintenance.security.LoginUser;
import com.carmaintenance.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer-portal")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerPortalController {

    private final CustomerService customerService;
    private final VehicleService vehicleService;
    private final RepairOrderService repairOrderService;
    private final RepairItemService repairItemService;
    private final PartUsageService partUsageService;
    private final ServiceReminderService reminderService;

    private Long getCurrentUserId() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return loginUser.getUser().getId();
    }

    private Customer getMyCustomer() {
        Long userId = getCurrentUserId();
        return customerService.getOne(new LambdaQueryWrapper<Customer>().eq(Customer::getUserId, userId));
    }

    /** 获取我的客户信息 */
    @GetMapping("/profile")
    public Result<?> getProfile() {
        Customer customer = getMyCustomer();
        if (customer == null) return Result.notFound("未找到客户信息");
        return Result.success(customer);
    }

    /** 获取我的车辆列表 */
    @GetMapping("/vehicles")
    public Result<?> getMyVehicles() {
        Customer customer = getMyCustomer();
        if (customer == null) return Result.success(List.of());
        List<Vehicle> vehicles = vehicleService.list(
            new LambdaQueryWrapper<Vehicle>().eq(Vehicle::getCustomerId, customer.getId()));
        return Result.success(vehicles);
    }

    /** 获取我的工单列表 */
    @GetMapping("/orders")
    public Result<?> getMyOrders(@RequestParam(required = false) Integer status) {
        Customer customer = getMyCustomer();
        if (customer == null) return Result.success(List.of());
        LambdaQueryWrapper<RepairOrder> wrapper = new LambdaQueryWrapper<RepairOrder>()
            .eq(RepairOrder::getCustomerId, customer.getId());
        if (status != null) wrapper.eq(RepairOrder::getStatus, status);
        wrapper.orderByDesc(RepairOrder::getCreatedAt);
        return Result.success(repairOrderService.list(wrapper));
    }

    /** 获取工单详情（含维修项目和配件使用） */
    @GetMapping("/orders/{id}")
    public Result<?> getOrderDetail(@PathVariable Long id) {
        Customer customer = getMyCustomer();
        RepairOrder order = repairOrderService.getById(id);
        if (order == null || customer == null || !order.getCustomerId().equals(customer.getId())) {
            return Result.notFound("工单不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("items", repairItemService.listByOrderId(id));
        data.put("partUsages", partUsageService.listByOrderId(id));
        return Result.success(data);
    }

    /** 创建预约工单 */
    @PostMapping("/orders")
    public Result<?> createAppointment(@RequestBody Map<String, Object> body) {
        Customer customer = getMyCustomer();
        if (customer == null) return Result.badRequest("未找到客户信息");

        Long vehicleId = Long.valueOf(body.get("vehicleId").toString());
        String faultDesc = (String) body.get("faultDesc");

        // 验证车辆属于该客户
        Vehicle vehicle = vehicleService.getById(vehicleId);
        if (vehicle == null || !vehicle.getCustomerId().equals(customer.getId())) {
            return Result.badRequest("车辆不存在或不属于您");
        }

        RepairOrder order = new RepairOrder();
        order.setOrderNo(repairOrderService.generateOrderNo());
        order.setCustomerId(customer.getId());
        order.setVehicleId(vehicleId);
        order.setFaultDesc(faultDesc);
        order.setIntakeMileage(vehicle.getMileage());
        order.setStatus(0);
        order.setIsPaid(0);
        repairOrderService.save(order);
        return Result.success("预约成功", order);
    }

    /** 支付工单 */
    @PutMapping("/orders/{id}/pay")
    public Result<?> payOrder(@PathVariable Long id, @Valid @RequestBody PaymentDTO dto) {
        Customer customer = getMyCustomer();
        RepairOrder order = repairOrderService.getById(id);
        if (order == null || customer == null || !order.getCustomerId().equals(customer.getId())) {
            return Result.notFound("工单不存在");
        }
        if (order.getStatus() != 3) return Result.badRequest("只有已完成的工单才能支付");
        if (order.getIsPaid() == 1) return Result.badRequest("该工单已支付");

        order.setIsPaid(1);
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setPaymentTime(LocalDateTime.now());
        repairOrderService.updateById(order);
        return Result.success("支付成功");
    }

    /** 获取我的提醒列表 */
    @GetMapping("/reminders")
    public Result<?> getMyReminders() {
        Customer customer = getMyCustomer();
        if (customer == null) return Result.success(List.of());
        List<ServiceReminder> reminders = reminderService.list(
            new LambdaQueryWrapper<ServiceReminder>()
                .eq(ServiceReminder::getCustomerId, customer.getId())
                .orderByDesc(ServiceReminder::getRemindDate));
        return Result.success(reminders);
    }

    /** 获取首页统计 */
    @GetMapping("/dashboard")
    public Result<?> getDashboard() {
        Customer customer = getMyCustomer();
        if (customer == null) return Result.success(Map.of());

        Long customerId = customer.getId();
        Map<String, Object> data = new HashMap<>();
        data.put("vehicleCount", vehicleService.count(
            new LambdaQueryWrapper<Vehicle>().eq(Vehicle::getCustomerId, customerId)));
        data.put("totalOrders", repairOrderService.count(
            new LambdaQueryWrapper<RepairOrder>().eq(RepairOrder::getCustomerId, customerId)));
        data.put("activeOrders", repairOrderService.count(
            new LambdaQueryWrapper<RepairOrder>().eq(RepairOrder::getCustomerId, customerId)
                .in(RepairOrder::getStatus, 0, 1, 2)));
        data.put("unpaidOrders", repairOrderService.count(
            new LambdaQueryWrapper<RepairOrder>().eq(RepairOrder::getCustomerId, customerId)
                .eq(RepairOrder::getStatus, 3).eq(RepairOrder::getIsPaid, 0)));
        data.put("pendingReminders", reminderService.count(
            new LambdaQueryWrapper<ServiceReminder>().eq(ServiceReminder::getCustomerId, customerId)
                .eq(ServiceReminder::getStatus, 0)));
        return Result.success(data);
    }
}
