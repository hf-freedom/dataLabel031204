package com.example.excel.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class TemplateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String templateId;

    private String templateName;

    private String templateCode;

    private String fileName;

    private String description;

    private List<Map<String, String>> headers;

    private Map<String, String> fieldMapping;

    private Class<?> dataClass;
}
