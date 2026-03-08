package com.carmaintenance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class VehicleDTO {
    private Long id;
    @NotNull(message = "客户ID不能为空")
    private Long customerId;
    @NotBlank(message = "车牌号不能为空")
    private String plateNumber;
    private String vin;
    @NotBlank(message = "品牌不能为空")
    private String brand;
    @NotBlank(message = "车型不能为空")
    private String model;
    private String color;
    private String engineNumber;
    private LocalDate purchaseDate;
    private Integer mileage;
    private LocalDate lastMaintenanceDate;
    private Integer lastMaintenanceMileage;
    private LocalDate insuranceExpireDate;
    private String remark;
}
