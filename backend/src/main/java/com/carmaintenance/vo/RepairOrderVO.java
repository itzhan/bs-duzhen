package com.carmaintenance.vo;

import com.carmaintenance.entity.RepairOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RepairOrderVO extends RepairOrder {
    private String customerName;
    private String plateNumber;
    private String advisorName;
    private String technicianName;
}
