package com.carmaintenance.controller;

import com.carmaintenance.common.Result;
import com.carmaintenance.dto.RepairItemDTO;
import com.carmaintenance.entity.RepairItem;
import com.carmaintenance.service.RepairItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/repair-items")
@RequiredArgsConstructor
public class RepairItemController {

    private final RepairItemService repairItemService;

    @GetMapping
    public Result<?> listByOrder(@RequestParam Long orderId) {
        return Result.success(repairItemService.listByOrderId(orderId));
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        RepairItem item = repairItemService.getById(id);
        if (item == null) {
            return Result.notFound("维修项目不存在");
        }
        return Result.success(item);
    }

    @PostMapping
    public Result<?> create(@Valid @RequestBody RepairItemDTO dto) {
        RepairItem item = new RepairItem();
        BeanUtils.copyProperties(dto, item);
        item.setStatus(0);
        // 计算小计
        BigDecimal hours = dto.getLaborHours() != null ? dto.getLaborHours() : BigDecimal.ZERO;
        BigDecimal price = dto.getLaborPrice() != null ? dto.getLaborPrice() : BigDecimal.ZERO;
        item.setAmount(hours.multiply(price));
        if (item.getItemType() == null) item.setItemType(1);
        if (item.getLaborHours() == null) item.setLaborHours(BigDecimal.ZERO);
        if (item.getLaborPrice() == null) item.setLaborPrice(BigDecimal.ZERO);

        repairItemService.save(item);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody RepairItemDTO dto) {
        RepairItem item = repairItemService.getById(id);
        if (item == null) {
            return Result.notFound("维修项目不存在");
        }
        BeanUtils.copyProperties(dto, item, "id");
        BigDecimal hours = item.getLaborHours() != null ? item.getLaborHours() : BigDecimal.ZERO;
        BigDecimal price = item.getLaborPrice() != null ? item.getLaborPrice() : BigDecimal.ZERO;
        item.setAmount(hours.multiply(price));
        repairItemService.updateById(item);
        return Result.success("更新成功");
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        RepairItem item = repairItemService.getById(id);
        if (item == null) {
            return Result.notFound("维修项目不存在");
        }
        item.setStatus(status);
        repairItemService.updateById(item);
        return Result.success("状态更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        repairItemService.removeById(id);
        return Result.success("删除成功");
    }
}
