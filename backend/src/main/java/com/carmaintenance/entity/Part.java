package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("part")
public class Part {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String partCode;

    private String partName;

    private String category;

    private String brand;

    private String specification;

    private String unit;

    private BigDecimal purchasePrice;

    private BigDecimal salePrice;

    private Integer stockQty;

    private Integer minStock;

    private String location;

    private Integer status;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private LocalDateTime deletedAt;
}
