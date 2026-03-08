package com.carmaintenance.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerDTO {
    private Long id;
    @NotBlank(message = "客户姓名不能为空")
    private String name;
    @NotBlank(message = "手机号不能为空")
    private String phone;
    private String email;
    private Integer gender;
    private String idCard;
    private String address;
    private String remark;
}
