package com.example.excel.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
public class OrgDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;

    @NotBlank(message = "机构编码不能为空")
    private String orgCode;

    @NotBlank(message = "机构名称不能为空")
    private String orgName;

    private String parentCode;

    private String orgType;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    private String contactPhone;

    private String contactPerson;

    private String address;

    private String status;

    private String remark;

    private Integer rowNum;

    private String validateMsg;
}
