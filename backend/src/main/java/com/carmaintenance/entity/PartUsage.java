package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("part_usage")
public class PartUsage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long partId;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal amount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
