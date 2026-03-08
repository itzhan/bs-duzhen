package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("inventory_record")
public class InventoryRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long partId;

    /** 类型: 1-入库, 2-出库, 3-盘点调整 */
    private Integer type;

    private Integer quantity;

    private Integer beforeQty;

    private Integer afterQty;

    private BigDecimal unitPrice;

    private Long relatedOrderId;

    private Long operatorId;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
