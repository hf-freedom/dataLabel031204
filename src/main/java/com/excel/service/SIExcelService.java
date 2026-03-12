package com.excel.service;

import com.excel.dto.OrganizationDTO;
import com.excel.dto.UserDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class SIExcelService {

    private static final String TEMPLATE_USER = "TEMPLATE_USER";
    private static final String TEMPLATE_ORG = "TEMPLATE_ORG";

    @Autowired
    private SICacheService cacheService;

    public Map<String, String> getTemplateList() {
        Map<String, String> templates = new LinkedHashMap<>();
        templates.put(TEMPLATE_USER, "用户模板");
        templates.put(TEMPLATE_ORG, "机构模板");
        return templates;
    }

    public byte[] downloadTemplate(String templateId) throws IOException {
        String fileName = getTemplateFileName(templateId);
        ClassPathResource resource = new ClassPathResource("config/" + fileName);
        
        if (!resource.exists()) {
            throw new RuntimeException("模板文件不存在: " + fileName);
        }
        
        InputStream is = resource.getInputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        is.close();
        return bos.toByteArray();
    }

    public String getTemplateFileName(String templateId) {
        switch (templateId) {
            case TEMPLATE_USER:
                return "user_template.xlsx";
            case TEMPLATE_ORG:
                return "org_template.xlsx";
            default:
                throw new RuntimeException("未知的模板ID: " + templateId);
        }
    }

    public Map<String, Object> parseExcel(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls"))) {
            throw new RuntimeException("请上传Excel文件(.xlsx或.xls格式)");
        }

        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        Row templateRow = sheet.getRow(0);
        if (templateRow == null) {
            throw new RuntimeException("Excel文件格式错误：第一行必须包含模板ID");
        }
        
        String templateId = getCellValue(templateRow.getCell(0));
        if (templateId == null || templateId.isEmpty()) {
            throw new RuntimeException("Excel文件格式错误：第一行第一列必须为模板ID");
        }

        Row headerRow = sheet.getRow(1);
        if (headerRow == null) {
            throw new RuntimeException("Excel文件格式错误：缺少表头行");
        }

        List<String> headers = new ArrayList<>();
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            headers.add(getCellValue(cell));
        }

        Map<Integer, Object> dataMap = new LinkedHashMap<>();
        int dataStartRow = 2;
        
        for (int i = dataStartRow; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }
            
            Object rowData = parseRowData(row, templateId, headers.size());
            if (rowData != null) {
                dataMap.put(i - dataStartRow, rowData);
            }
        }

        workbook.close();

        String taskId = cacheService.generateTaskId();
        cacheService.saveTaskData(taskId, templateId, headers, dataMap);

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        result.put("templateId", templateId);
        result.put("templateName", getTemplateName(templateId));
        result.put("headers", headers);
        result.put("data", cacheService.getTaskDataList(taskId));
        result.put("total", dataMap.size());

        return result;
    }

    private Object parseRowData(Row row, String templateId, int columnCount) {
        String[] values = new String[columnCount];
        boolean hasData = false;
        
        for (int i = 0; i < columnCount; i++) {
            Cell cell = row.getCell(i);
            values[i] = getCellValue(cell);
            if (values[i] != null && !values[i].isEmpty()) {
                hasData = true;
            }
        }
        
        if (!hasData) {
            return null;
        }

        switch (templateId) {
            case TEMPLATE_USER:
                return parseUserDTO(values);
            case TEMPLATE_ORG:
                return parseOrganizationDTO(values);
            default:
                return Arrays.asList(values);
        }
    }

    private UserDTO parseUserDTO(String[] values) {
        UserDTO user = new UserDTO();
        user.setId(getValue(values, 0));
        user.setUsername(getValue(values, 1));
        user.setRealName(getValue(values, 2));
        user.setEmail(getValue(values, 3));
        user.setPhone(getValue(values, 4));
        user.setDepartment(getValue(values, 5));
        user.setStatus(getValue(values, 6));
        return user;
    }

    private OrganizationDTO parseOrganizationDTO(String[] values) {
        OrganizationDTO org = new OrganizationDTO();
        org.setId(getValue(values, 0));
        org.setOrgCode(getValue(values, 1));
        org.setOrgName(getValue(values, 2));
        org.setOrgType(getValue(values, 3));
        org.setAddress(getValue(values, 4));
        org.setContactPerson(getValue(values, 5));
        org.setContactPhone(getValue(values, 6));
        org.setStatus(getValue(values, 7));
        return org;
    }

    private String getValue(String[] values, int index) {
        if (index < values.length) {
            return values[index];
        }
        return null;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double value = cell.getNumericCellValue();
                    if (value == (long) value) {
                        return String.valueOf((long) value);
                    }
                    return String.valueOf(value);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    private String getTemplateName(String templateId) {
        switch (templateId) {
            case TEMPLATE_USER:
                return "用户模板";
            case TEMPLATE_ORG:
                return "机构模板";
            default:
                return "未知模板";
        }
    }

    public Map<String, Object> getPreviewData(String taskId) {
        if (!cacheService.existsTask(taskId)) {
            throw new RuntimeException("任务不存在或已过期: " + taskId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        result.put("templateId", cacheService.getTaskTemplate(taskId));
        result.put("templateName", getTemplateName(cacheService.getTaskTemplate(taskId)));
        result.put("headers", cacheService.getTaskHeaders(taskId));
        result.put("data", cacheService.getTaskDataList(taskId));
        result.put("total", cacheService.getTaskData(taskId).size());

        return result;
    }

    public void deleteRow(String taskId, Integer rowIndex) {
        if (!cacheService.existsTask(taskId)) {
            throw new RuntimeException("任务不存在或已过期: " + taskId);
        }
        cacheService.deleteRowData(taskId, rowIndex);
    }

    public void updateRow(String taskId, Integer rowIndex, Object rowData) {
        if (!cacheService.existsTask(taskId)) {
            throw new RuntimeException("任务不存在或已过期: " + taskId);
        }
        cacheService.updateRowData(taskId, rowIndex, rowData);
    }

    public List<Object> saveData(String taskId) {
        if (!cacheService.existsTask(taskId)) {
            throw new RuntimeException("任务不存在或已过期: " + taskId);
        }
        
        List<Object> data = cacheService.getTaskDataList(taskId);
        
        cacheService.clearTask(taskId);
        
        return data;
    }
}
