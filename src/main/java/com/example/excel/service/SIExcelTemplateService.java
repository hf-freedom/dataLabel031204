package com.example.excel.service;

import com.example.excel.dto.TemplateDTO;
import com.example.excel.util.ExcelUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
@Service
public class SIExcelTemplateService {

    @Value("${excel.template.path:config/}")
    private String templatePath;

    private final Map<String, TemplateDTO> templateRegistry = new HashMap<>();

    @PostConstruct
    public void init() {
        registerUserTemplate();
        registerOrgTemplate();
        generateTemplateFiles();
    }

    private void registerUserTemplate() {
        TemplateDTO template = new TemplateDTO();
        template.setTemplateId("USER_TEMPLATE_001");
        template.setTemplateName("用户导入模板");
        template.setTemplateCode("USER");
        template.setFileName("用户导入模板.xlsx");
        template.setDescription("用于批量导入用户数据");

        List<Map<String, String>> headers = new ArrayList<>();
        headers.add(createHeader("username", "用户名(*)"));
        headers.add(createHeader("realName", "真实姓名(*)"));
        headers.add(createHeader("phone", "手机号(*)"));
        headers.add(createHeader("email", "邮箱"));
        headers.add(createHeader("department", "部门"));
        headers.add(createHeader("status", "状态"));
        headers.add(createHeader("remark", "备注"));
        template.setHeaders(headers);

        Map<String, String> fieldMapping = new LinkedHashMap<>();
        fieldMapping.put("用户名(*)", "username");
        fieldMapping.put("真实姓名(*)", "realName");
        fieldMapping.put("手机号(*)", "phone");
        fieldMapping.put("邮箱", "email");
        fieldMapping.put("部门", "department");
        fieldMapping.put("状态", "status");
        fieldMapping.put("备注", "remark");
        template.setFieldMapping(fieldMapping);

        templateRegistry.put(template.getTemplateId(), template);
    }

    private void registerOrgTemplate() {
        TemplateDTO template = new TemplateDTO();
        template.setTemplateId("ORG_TEMPLATE_001");
        template.setTemplateName("机构导入模板");
        template.setTemplateCode("ORG");
        template.setFileName("机构导入模板.xlsx");
        template.setDescription("用于批量导入机构数据");

        List<Map<String, String>> headers = new ArrayList<>();
        headers.add(createHeader("orgCode", "机构编码(*)"));
        headers.add(createHeader("orgName", "机构名称(*)"));
        headers.add(createHeader("parentCode", "上级机构编码"));
        headers.add(createHeader("orgType", "机构类型"));
        headers.add(createHeader("contactPhone", "联系电话"));
        headers.add(createHeader("contactPerson", "联系人"));
        headers.add(createHeader("address", "地址"));
        headers.add(createHeader("status", "状态"));
        headers.add(createHeader("remark", "备注"));
        template.setHeaders(headers);

        Map<String, String> fieldMapping = new LinkedHashMap<>();
        fieldMapping.put("机构编码(*)", "orgCode");
        fieldMapping.put("机构名称(*)", "orgName");
        fieldMapping.put("上级机构编码", "parentCode");
        fieldMapping.put("机构类型", "orgType");
        fieldMapping.put("联系电话", "contactPhone");
        fieldMapping.put("联系人", "contactPerson");
        fieldMapping.put("地址", "address");
        fieldMapping.put("状态", "status");
        fieldMapping.put("备注", "remark");
        template.setFieldMapping(fieldMapping);

        templateRegistry.put(template.getTemplateId(), template);
    }

    private Map<String, String> createHeader(String field, String title) {
        Map<String, String> header = new HashMap<>();
        header.put("field", field);
        header.put("title", title);
        return header;
    }

    private void generateTemplateFiles() {
        for (TemplateDTO template : templateRegistry.values()) {
            try {
                File templateFile = new File(templatePath + template.getTemplateCode().toLowerCase() + "_template.xlsx");
                if (!templateFile.exists()) {
                    templateFile.getParentFile().mkdirs();

                    List<String> headerTitles = new ArrayList<>();
                    for (Map<String, String> header : template.getHeaders()) {
                        headerTitles.add(header.get("title"));
                    }

                    Workbook workbook = ExcelUtil.createTemplateWorkbook(
                            template.getTemplateId(),
                            template.getTemplateName(),
                            headerTitles,
                            template.getFieldMapping()
                    );

                    try (FileOutputStream fos = new FileOutputStream(templateFile)) {
                        workbook.write(fos);
                    }
                    log.info("Generated template file: {}", templateFile.getAbsolutePath());
                }
            } catch (Exception e) {
                log.error("Failed to generate template file for: {}", template.getTemplateId(), e);
            }
        }
    }

    public List<TemplateDTO> getAllTemplates() {
        return new ArrayList<>(templateRegistry.values());
    }

    public TemplateDTO getTemplateById(String templateId) {
        return templateRegistry.get(templateId);
    }

    public void downloadTemplate(String templateId, HttpServletResponse response) throws IOException {
        TemplateDTO template = templateRegistry.get(templateId);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + templateId);
        }

        File templateFile = new File(templatePath + template.getTemplateCode().toLowerCase() + "_template.xlsx");

        // 如果文件不存在或为空，则动态生成
        if (templateFile.exists() && templateFile.length() > 0) {
            try (FileInputStream fis = new FileInputStream(templateFile);
                 Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(fis)) {
                ExcelUtil.downloadTemplate(response, template.getFileName(), workbook);
                return;
            } catch (Exception e) {
                log.warn("Failed to read template file, generating new one: {}", e.getMessage());
            }
        }

        // 动态生成模板
        List<String> headerTitles = new ArrayList<>();
        for (Map<String, String> header : template.getHeaders()) {
            headerTitles.add(header.get("title"));
        }

        Workbook workbook = ExcelUtil.createTemplateWorkbook(
                template.getTemplateId(),
                template.getTemplateName(),
                headerTitles,
                template.getFieldMapping()
        );
        ExcelUtil.downloadTemplate(response, template.getFileName(), workbook);
    }

    public TemplateDTO getTemplateByCode(String templateCode) {
        return templateRegistry.values().stream()
                .filter(t -> t.getTemplateCode().equalsIgnoreCase(templateCode))
                .findFirst()
                .orElse(null);
    }
}
