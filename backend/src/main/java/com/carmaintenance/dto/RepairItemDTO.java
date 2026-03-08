package com.carmaintenance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class RepairItemDTO {
    private Long id;
    @NotNull(message = "工单ID不能为空")
    private Long orderId;
    @NotBlank(message = "维修项目名称不能为空")
    private String itemName;
    private Integer itemType;
    private BigDecimal laborHours;
    private BigDecimal laborPrice;
    private String remark;
}
