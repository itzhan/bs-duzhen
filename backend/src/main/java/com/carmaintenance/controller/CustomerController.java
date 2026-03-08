package com.carmaintenance.controller;

import com.carmaintenance.common.PageResult;
import com.carmaintenance.common.Result;
import com.carmaintenance.dto.CustomerDTO;
import com.carmaintenance.entity.Customer;
import com.carmaintenance.security.LoginUser;
import com.carmaintenance.service.CustomerService;
import com.carmaintenance.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final VehicleService vehicleService;

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword) {
        return Result.success(PageResult.of(customerService.pageList(page, size, keyword)));
    }

    @GetMapping("/all")
    public Result<?> listAll() {
        return Result.success(customerService.list());
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        Customer customer = customerService.getById(id);
        if (customer == null) {
            return Result.notFound("客户不存在");
        }
        return Result.success(customer);
    }

    @GetMapping("/{id}/vehicles")
    public Result<?> getVehicles(@PathVariable Long id) {
        return Result.success(vehicleService.listByCustomerId(id));
    }

    @PostMapping
    public Result<?> create(@Valid @RequestBody CustomerDTO dto) {
        Customer customer = new Customer();
        BeanUtils.copyProperties(dto, customer);

        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        customer.setCreatedBy(loginUser.getUser().getId());

        customerService.save(customer);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody CustomerDTO dto) {
        Customer customer = customerService.getById(id);
        if (customer == null) {
            return Result.notFound("客户不存在");
        }
        BeanUtils.copyProperties(dto, customer, "id");
        customerService.updateById(customer);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        customerService.removeById(id);
        return Result.success("删除成功");
    }
}
