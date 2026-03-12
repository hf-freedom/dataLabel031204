package com.example.excel.api;

import com.example.excel.dto.TemplateDTO;
import com.example.excel.service.SIExcelTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/template")
public class ExcelTemplateController {

    @Autowired
    private SIExcelTemplateService templateService;

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listTemplates() {
        List<TemplateDTO> templates = templateService.getAllTemplates();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", templates);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/download/{templateId}")
    public void downloadTemplate(@PathVariable String templateId, HttpServletResponse response) {
        try {
            templateService.downloadTemplate(templateId, response);
        } catch (IOException e) {
            log.error("Failed to download template: {}", templateId, e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/info/{templateId}")
    public ResponseEntity<Map<String, Object>> getTemplateInfo(@PathVariable String templateId) {
        TemplateDTO template = templateService.getTemplateById(templateId);
        Map<String, Object> result = new HashMap<>();
        if (template != null) {
            result.put("success", true);
            result.put("data", template);
        } else {
            result.put("success", false);
            result.put("message", "模板不存在");
        }
        return ResponseEntity.ok(result);
    }
}
