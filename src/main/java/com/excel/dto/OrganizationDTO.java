package com.excel.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class OrganizationDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String orgCode;
    private String orgName;
    private String orgType;
    private String address;
    private String contactPerson;
    private String contactPhone;
    private String status;
    
    public OrganizationDTO() {
    }
    
    public OrganizationDTO(String id, String orgCode, String orgName, String orgType, String address, String contactPerson, String contactPhone, String status) {
        this.id = id;
        this.orgCode = orgCode;
        this.orgName = orgName;
        this.orgType = orgType;
        this.address = address;
        this.contactPerson = contactPerson;
        this.contactPhone = contactPhone;
        this.status = status;
    }
}
