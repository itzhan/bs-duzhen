package com.carmaintenance.controller;

import com.carmaintenance.common.PageResult;
import com.carmaintenance.common.Result;
import com.carmaintenance.dto.VehicleDTO;
import com.carmaintenance.entity.Vehicle;
import com.carmaintenance.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Long customerId) {
        return Result.success(PageResult.of(vehicleService.pageList(page, size, keyword, customerId)));
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        Vehicle vehicle = vehicleService.getById(id);
        if (vehicle == null) {
            return Result.notFound("车辆不存在");
        }
        return Result.success(vehicle);
    }

    @PostMapping
    public Result<?> create(@Valid @RequestBody VehicleDTO dto) {
        Vehicle vehicle = new Vehicle();
        BeanUtils.copyProperties(dto, vehicle);
        vehicleService.save(vehicle);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody VehicleDTO dto) {
        Vehicle vehicle = vehicleService.getById(id);
        if (vehicle == null) {
            return Result.notFound("车辆不存在");
        }
        BeanUtils.copyProperties(dto, vehicle, "id");
        vehicleService.updateById(vehicle);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        vehicleService.removeById(id);
        return Result.success("删除成功");
    }
}
