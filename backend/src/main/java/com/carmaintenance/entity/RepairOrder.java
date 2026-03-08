package com.carmaintenance.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("repair_order")
public class RepairOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long customerId;

    private Long vehicleId;

    private Long technicianId;

    /** 状态: 0-待接单, 1-维修中, 2-待质检, 3-已完成, 4-已取消 */
    private Integer status;

    private String faultDesc;

    private String diagnosis;

    private Integer intakeMileage;

    private LocalDateTime estimatedFinishTime;

    private LocalDateTime actualFinishTime;

    private BigDecimal laborCost;

    private BigDecimal partsCost;

    private BigDecimal totalCost;

    /** 0-未结算, 1-已结算 */
    private Integer isPaid;

    private String paymentMethod;

    private LocalDateTime paymentTime;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private LocalDateTime deletedAt;
}
