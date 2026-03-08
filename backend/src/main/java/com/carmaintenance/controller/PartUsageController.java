package com.carmaintenance.controller;

import com.carmaintenance.common.Result;
import com.carmaintenance.dto.PartUsageDTO;
import com.carmaintenance.entity.Part;
import com.carmaintenance.entity.PartUsage;
import com.carmaintenance.security.LoginUser;
import com.carmaintenance.service.PartService;
import com.carmaintenance.service.PartUsageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/part-usages")
@RequiredArgsConstructor
public class PartUsageController {

    private final PartUsageService partUsageService;
    private final PartService partService;

    @GetMapping
    public Result<?> listByOrder(@RequestParam Long orderId) {
        return Result.success(partUsageService.listByOrderId(orderId));
    }

    @PostMapping
    public Result<?> create(@Valid @RequestBody PartUsageDTO dto) {
        Part part = partService.getById(dto.getPartId());
        if (part == null) {
            return Result.notFound("配件不存在");
        }

        BigDecimal unitPrice = dto.getUnitPrice() != null ? dto.getUnitPrice() : part.getSalePrice();

        PartUsage usage = new PartUsage();
        usage.setOrderId(dto.getOrderId());
        usage.setPartId(dto.getPartId());
        usage.setQuantity(dto.getQuantity());
        usage.setUnitPrice(unitPrice);
        usage.setAmount(unitPrice.multiply(BigDecimal.valueOf(dto.getQuantity())));

        partUsageService.save(usage);

        // 扣减库存
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        partService.adjustStock(dto.getPartId(), dto.getQuantity(), 2,
                loginUser.getUser().getId(), dto.getOrderId(), "工单用料出库");

        return Result.success("配件使用记录创建成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        PartUsage usage = partUsageService.getById(id);
        if (usage == null) {
            return Result.notFound("记录不存在");
        }
        partUsageService.removeById(id);

        // 归还库存
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        partService.adjustStock(usage.getPartId(), usage.getQuantity(), 1,
                loginUser.getUser().getId(), usage.getOrderId(), "工单用料退回");

        return Result.success("删除成功");
    }
}
