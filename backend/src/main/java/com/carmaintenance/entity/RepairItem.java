package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("repair_item")
public class RepairItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private String itemName;

    /** 类型: 1-维修, 2-保养, 3-钣喷, 4-其他 */
    private Integer itemType;

    private BigDecimal laborHours;

    private BigDecimal laborPrice;

    private BigDecimal amount;

    /** 状态: 0-待开始, 1-进行中, 2-已完成 */
    private Integer status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
