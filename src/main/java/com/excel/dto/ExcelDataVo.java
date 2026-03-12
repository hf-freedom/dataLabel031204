package com.excel.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import java.util.Map;

@ApiModel("Excel数据视图对象")
public class ExcelDataVo {
    @ApiModelProperty("任务ID")
    private String taskId;
    
    @ApiModelProperty("模板ID")
    private String templateId;
    
    @ApiModelProperty("表头")
    private List<String> headers;
    
    @ApiModelProperty("数据列表")
    private List<Map<String, Object>> dataList;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public List<Map<String, Object>> getDataList() {
        return dataList;
    }

    public void setDataList(List<Map<String, Object>> dataList) {
        this.dataList = dataList;
    }
}
