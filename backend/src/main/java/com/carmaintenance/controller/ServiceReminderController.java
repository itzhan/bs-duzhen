package com.carmaintenance.controller;

import com.carmaintenance.common.PageResult;
import com.carmaintenance.common.Result;
import com.carmaintenance.dto.ServiceReminderDTO;
import com.carmaintenance.entity.ServiceReminder;
import com.carmaintenance.security.LoginUser;
import com.carmaintenance.service.ServiceReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ServiceReminderController {

    private final ServiceReminderService reminderService;

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) Integer type,
                          @RequestParam(required = false) Integer status,
                          @RequestParam(required = false) Long customerId) {
        return Result.success(PageResult.of(reminderService.pageList(page, size, type, status, customerId)));
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        ServiceReminder reminder = reminderService.getById(id);
        if (reminder == null) {
            return Result.notFound("提醒不存在");
        }
        return Result.success(reminder);
    }

    @PostMapping
    public Result<?> create(@Valid @RequestBody ServiceReminderDTO dto) {
        ServiceReminder reminder = new ServiceReminder();
        BeanUtils.copyProperties(dto, reminder);
        reminder.setStatus(0);

        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        reminder.setCreatedBy(loginUser.getUser().getId());

        reminderService.save(reminder);
        return Result.success("创建成功");
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody ServiceReminderDTO dto) {
        ServiceReminder reminder = reminderService.getById(id);
        if (reminder == null) {
            return Result.notFound("提醒不存在");
        }
        BeanUtils.copyProperties(dto, reminder, "id");
        reminderService.updateById(reminder);
        return Result.success("更新成功");
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        ServiceReminder reminder = reminderService.getById(id);
        if (reminder == null) {
            return Result.notFound("提醒不存在");
        }
        reminder.setStatus(status);
        reminderService.updateById(reminder);
        return Result.success("状态更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        reminderService.removeById(id);
        return Result.success("删除成功");
    }
}
