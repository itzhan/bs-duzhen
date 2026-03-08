package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("service_reminder")
public class ServiceReminder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long customerId;

    private Long technicianId;

    private Long vehicleId;

    /** 类型: 1-定期保养, 2-保险到期, 3-维修进度, 4-其他 */
    private Integer type;

    private String title;

    private String content;

    private LocalDate remindDate;

    /** 状态: 0-待发送, 1-已发送, 2-已确认 */
    private Integer status;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
