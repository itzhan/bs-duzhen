package com.carmaintenance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class InventoryRecordDTO {
    @NotNull(message = "配件ID不能为空")
    private Long partId;
    @NotNull(message = "操作类型不能为空")
    private Integer type;
    @NotNull(message = "数量不能为空")
    private Integer quantity;
    private BigDecimal unitPrice;
    private Long relatedOrderId;
    private String remark;
}
