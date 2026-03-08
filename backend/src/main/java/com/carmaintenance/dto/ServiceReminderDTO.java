package com.carmaintenance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ServiceReminderDTO {
    private Long id;
    @NotNull(message = "客户ID不能为空")
    private Long customerId;
    @NotNull(message = "车辆ID不能为空")
    private Long vehicleId;
    @NotNull(message = "提醒类型不能为空")
    private Integer type;
    @NotBlank(message = "提醒标题不能为空")
    private String title;
    private String content;
    @NotNull(message = "提醒日期不能为空")
    private LocalDate remindDate;
}
