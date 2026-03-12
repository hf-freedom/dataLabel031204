package com.excel.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("用户信息")
public class UserDto {
    @ApiModelProperty("用户名")
    private String username;
    
    @ApiModelProperty("年龄")
    private Integer age;
    
    @ApiModelProperty("邮箱")
    private String email;
    
    @ApiModelProperty("手机号")
    private String phone;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
