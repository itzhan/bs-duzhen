package com.carmaintenance.controller;

import com.carmaintenance.common.PageResult;
import com.carmaintenance.common.Result;
import com.carmaintenance.dto.PartDTO;
import com.carmaintenance.dto.InventoryRecordDTO;
import com.carmaintenance.entity.Part;
import com.carmaintenance.security.LoginUser;
import com.carmaintenance.service.InventoryRecordService;
import com.carmaintenance.service.PartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartController {

    private final PartService partService;
    private final InventoryRecordService inventoryRecordService;

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String category) {
        return Result.success(PageResult.of(partService.pageList(page, size, keyword, category)));
    }

    @GetMapping("/all")
    public Result<?> listAll() {
        return Result.success(partService.list());
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        Part part = partService.getById(id);
        if (part == null) {
            return Result.notFound("配件不存在");
        }
        return Result.success(part);
    }

    @PostMapping
    public Result<?> create(@Valid @RequestBody PartDTO dto) {
        Part part = new Part();
        BeanUtils.copyProperties(dto, part);
        if (part.getStatus() == null) part.setStatus(1);
        partService.save(part);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody PartDTO dto) {
        Part part = partService.getById(id);
        if (part == null) {
            return Result.notFound("配件不存在");
        }
        BeanUtils.copyProperties(dto, part, "id", "stockQty");
        partService.updateById(part);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        partService.removeById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/low-stock")
    public Result<?> getLowStockParts() {
        return Result.success(partService.getLowStockParts());
    }

    @PostMapping("/stock-adjust")
    public Result<?> adjustStock(@Valid @RequestBody InventoryRecordDTO dto) {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        partService.adjustStock(dto.getPartId(), dto.getQuantity(), dto.getType(),
                loginUser.getUser().getId(), dto.getRelatedOrderId(), dto.getRemark());
        return Result.success("库存调整成功");
    }

    @GetMapping("/inventory-records")
    public Result<?> getInventoryRecords(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(required = false) Long partId,
                                          @RequestParam(required = false) Integer type) {
        return Result.success(PageResult.of(inventoryRecordService.pageList(page, size, partId, type)));
    }
}
