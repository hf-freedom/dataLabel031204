package com.example.excel.service;

import com.example.excel.cache.TaskCacheManager;
import com.example.excel.dto.*;
import com.example.excel.util.ExcelUtil;
import com.example.excel.util.ValidationUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SIExcelPreviewService {

    @Autowired
    private TaskCacheManager taskCacheManager;

    @Autowired
    private SIExcelTemplateService templateService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExcelTaskDTO uploadAndPreview(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        log.info("Uploading file: {}", fileName);

        String templateId;
        try (InputStream is = file.getInputStream()) {
            templateId = ExcelUtil.readTemplateId(is);
        }

        if (templateId == null || templateId.isEmpty()) {
            throw new IllegalArgumentException("无法识别模板ID，请使用正确的模板文件");
        }

        TemplateDTO template = templateService.getTemplateById(templateId);
        if (template == null) {
            throw new IllegalArgumentException("未知的模板ID: " + templateId);
        }

        List<Map<String, Object>> dataList;
        try (InputStream is = file.getInputStream()) {
            dataList = ExcelUtil.readExcelData(is, template.getFieldMapping());
        }

        String taskId = taskCacheManager.createTaskId();

        validateData(dataList, templateId);

        ExcelTaskDTO task = new ExcelTaskDTO();
        task.setTaskId(taskId);
        task.setTemplateId(templateId);
        task.setTemplateName(template.getTemplateName());
        task.setFileName(fileName);
        task.setTotalCount(dataList.size());
        task.setDataList(dataList);
        task.setHeaders(template.getHeaders().stream()
                .map(h -> h.get("title"))
                .collect(Collectors.toList()));
        task.setFieldMapping(template.getFieldMapping());
        task.setStatus("PREVIEW");

        int validCount = (int) dataList.stream()
                .filter(d -> d.get("_validateMsg") == null || ((String) d.get("_validateMsg")).isEmpty())
                .count();
        task.setValidCount(validCount);
        task.setInvalidCount(dataList.size() - validCount);

        taskCacheManager.putTask(taskId, task);

        log.info("Task created: {}, total: {}, valid: {}, invalid: {}",
                taskId, dataList.size(), validCount, dataList.size() - validCount);

        return task;
    }

    private void validateData(List<Map<String, Object>> dataList, String templateId) {
        for (Map<String, Object> row : dataList) {
            List<String> errors = new ArrayList<>();

            if ("USER_TEMPLATE_001".equals(templateId)) {
                validateUserData(row, errors);
            } else if ("ORG_TEMPLATE_001".equals(templateId)) {
                validateOrgData(row, errors);
            }

            if (!errors.isEmpty()) {
                row.put("_validateMsg", String.join("; ", errors));
                row.put("_valid", false);
            } else {
                row.put("_validateMsg", "");
                row.put("_valid", true);
            }
        }
    }

    private void validateUserData(Map<String, Object> row, List<String> errors) {
        String username = (String) row.get("username");
        if (username == null || username.trim().isEmpty()) {
            errors.add("用户名不能为空");
        }

        String realName = (String) row.get("realName");
        if (realName == null || realName.trim().isEmpty()) {
            errors.add("真实姓名不能为空");
        }

        String phone = (String) row.get("phone");
        if (phone == null || phone.trim().isEmpty()) {
            errors.add("手机号不能为空");
        } else if (!phone.matches("^1[3-9]\\d{9}$")) {
            errors.add("手机号格式不正确");
        }

        String email = (String) row.get("email");
        if (email != null && !email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            errors.add("邮箱格式不正确");
        }
    }

    private void validateOrgData(Map<String, Object> row, List<String> errors) {
        String orgCode = (String) row.get("orgCode");
        if (orgCode == null || orgCode.trim().isEmpty()) {
            errors.add("机构编码不能为空");
        }

        String orgName = (String) row.get("orgName");
        if (orgName == null || orgName.trim().isEmpty()) {
            errors.add("机构名称不能为空");
        }

        String contactPhone = (String) row.get("contactPhone");
        if (contactPhone != null && !contactPhone.isEmpty() && !contactPhone.matches("^1[3-9]\\d{9}$")) {
            errors.add("联系电话格式不正确");
        }
    }

    public ExcelTaskDTO getPreviewData(String taskId) {
        ExcelTaskDTO task = taskCacheManager.getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在或已过期: " + taskId);
        }
        return task;
    }

    public ExcelTaskDTO updateRow(String taskId, int rowIndex, Map<String, Object> rowData) {
        ExcelTaskDTO task = taskCacheManager.getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在或已过期: " + taskId);
        }

        List<Map<String, Object>> dataList = task.getDataList();
        if (rowIndex < 0 || rowIndex >= dataList.size()) {
            throw new IllegalArgumentException("行索引超出范围: " + rowIndex);
        }

        Map<String, Object> oldRow = dataList.get(rowIndex);
        rowData.put("_rowNum", oldRow.get("_rowNum"));

        List<String> errors = new ArrayList<>();
        if ("USER_TEMPLATE_001".equals(task.getTemplateId())) {
            validateUserData(rowData, errors);
        } else if ("ORG_TEMPLATE_001".equals(task.getTemplateId())) {
            validateOrgData(rowData, errors);
        }

        if (!errors.isEmpty()) {
            rowData.put("_validateMsg", String.join("; ", errors));
            rowData.put("_valid", false);
        } else {
            rowData.put("_validateMsg", "");
            rowData.put("_valid", true);
        }

        dataList.set(rowIndex, rowData);

        int validCount = (int) dataList.stream()
                .filter(d -> d.get("_validateMsg") == null || ((String) d.get("_validateMsg")).isEmpty())
                .count();
        task.setValidCount(validCount);
        task.setInvalidCount(dataList.size() - validCount);

        taskCacheManager.updateTask(taskId, task);

        return task;
    }

    public ExcelTaskDTO deleteRow(String taskId, int rowIndex) {
        ExcelTaskDTO task = taskCacheManager.getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在或已过期: " + taskId);
        }

        List<Map<String, Object>> dataList = task.getDataList();
        if (rowIndex < 0 || rowIndex >= dataList.size()) {
            throw new IllegalArgumentException("行索引超出范围: " + rowIndex);
        }

        dataList.remove(rowIndex);
        task.setTotalCount(dataList.size());

        int validCount = (int) dataList.stream()
                .filter(d -> d.get("_validateMsg") == null || ((String) d.get("_validateMsg")).isEmpty())
                .count();
        task.setValidCount(validCount);
        task.setInvalidCount(dataList.size() - validCount);

        taskCacheManager.updateTask(taskId, task);

        return task;
    }

    public Map<String, Object> saveData(String taskId) {
        ExcelTaskDTO task = taskCacheManager.getTask(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在或已过期: " + taskId);
        }

        List<Map<String, Object>> dataList = task.getDataList();
        List<Map<String, Object>> invalidRows = dataList.stream()
                .filter(d -> d.get("_validateMsg") != null && !((String) d.get("_validateMsg")).isEmpty())
                .collect(Collectors.toList());

        if (!invalidRows.isEmpty()) {
            throw new IllegalStateException("存在" + invalidRows.size() + "条数据校验不通过，请先修正");
        }

        task.setStatus("SAVED");
        taskCacheManager.updateTask(taskId, task);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("taskId", taskId);
        result.put("savedCount", dataList.size());
        result.put("message", "成功保存 " + dataList.size() + " 条数据");

        log.info("Data saved for task: {}, count: {}", taskId, dataList.size());

        return result;
    }

    public void clearTask(String taskId) {
        taskCacheManager.removeTask(taskId);
        log.info("Task cleared: {}", taskId);
    }
}
