package com.carmaintenance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RepairOrderDTO {
    private Long id;
    @NotNull(message = "客户ID不能为空")
    private Long customerId;
    @NotNull(message = "车辆ID不能为空")
    private Long vehicleId;
    private Long technicianId;
    private String faultDesc;
    private String diagnosis;
    private Integer intakeMileage;
    private LocalDateTime estimatedFinishTime;
    private String remark;
}
