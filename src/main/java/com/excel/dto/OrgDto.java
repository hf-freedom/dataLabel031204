package com.excel.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("机构信息")
public class OrgDto {
    @ApiModelProperty("机构名称")
    private String orgName;
    
    @ApiModelProperty("机构代码")
    private String orgCode;
    
    @ApiModelProperty("联系人")
    private String contact;
    
    @ApiModelProperty("联系电话")
    private String contactPhone;

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
}
