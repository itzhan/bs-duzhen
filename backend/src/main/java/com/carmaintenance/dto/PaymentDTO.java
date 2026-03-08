package com.carmaintenance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaymentDTO {
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;
}
