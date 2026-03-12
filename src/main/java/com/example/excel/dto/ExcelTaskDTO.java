package com.example.excel.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ExcelTaskDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String taskId;

    private String templateId;

    private String templateName;

    private String fileName;

    private Integer totalCount;

    private Integer validCount;

    private Integer invalidCount;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime expireTime;

    private List<Map<String, Object>> dataList;

    private List<String> headers;

    private Map<String, String> fieldMapping;
}
