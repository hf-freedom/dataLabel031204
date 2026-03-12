package com.excel.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String department;
    private String status;
    
    public UserDTO() {
    }
    
    public UserDTO(String id, String username, String realName, String email, String phone, String department, String status) {
        this.id = id;
        this.username = username;
        this.realName = realName;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.status = status;
    }
}
