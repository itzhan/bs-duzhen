package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("vehicle")
public class Vehicle {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long customerId;

    private String plateNumber;

    private String vin;

    private String brand;

    private String model;

    private String color;

    private String engineNumber;

    private LocalDate purchaseDate;

    private Integer mileage;

    private LocalDate lastMaintenanceDate;

    private Integer lastMaintenanceMileage;

    private LocalDate insuranceExpireDate;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private LocalDateTime deletedAt;
}
