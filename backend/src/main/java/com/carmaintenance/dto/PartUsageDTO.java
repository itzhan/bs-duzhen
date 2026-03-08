package com.carmaintenance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PartUsageDTO {
    private Long id;
    @NotNull(message = "工单ID不能为空")
    private Long orderId;
    @NotNull(message = "配件ID不能为空")
    private Long partId;
    @NotNull(message = "数量不能为空")
    private Integer quantity;
    private BigDecimal unitPrice;
}
