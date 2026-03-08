package com.carmaintenance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PartDTO {
    private Long id;
    @NotBlank(message = "配件编号不能为空")
    private String partCode;
    @NotBlank(message = "配件名称不能为空")
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
}
