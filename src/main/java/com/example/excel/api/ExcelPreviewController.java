package com.example.excel.api;

import com.example.excel.dto.ExcelTaskDTO;
import com.example.excel.service.SIExcelPreviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/preview")
public class ExcelPreviewController {

    @Autowired
    private SIExcelPreviewService previewService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadExcel(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            ExcelTaskDTO task = previewService.uploadAndPreview(file);
            result.put("success", true);
            result.put("data", task);
            result.put("message", "上传成功，共 " + task.getTotalCount() + " 条数据");
        } catch (IllegalArgumentException e) {
            log.warn("Upload failed: {}", e.getMessage());
            result.put("success", false);
            result.put("message", e.getMessage());
        } catch (IOException e) {
            log.error("Upload failed", e);
            result.put("success", false);
            result.put("message", "文件读取失败: " + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/data/{taskId}")
    public ResponseEntity<Map<String, Object>> getPreviewData(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        try {
            ExcelTaskDTO task = previewService.getPreviewData(taskId);
            result.put("success", true);
            result.put("data", task);
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/update/{taskId}")
    public ResponseEntity<Map<String, Object>> updateRow(
            @PathVariable String taskId,
            @RequestParam("rowIndex") int rowIndex,
            @RequestBody Map<String, Object> rowData) {
        Map<String, Object> result = new HashMap<>();
        try {
            ExcelTaskDTO task = previewService.updateRow(taskId, rowIndex, rowData);
            result.put("success", true);
            result.put("data", task);
            result.put("message", "更新成功");
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/delete/{taskId}")
    public ResponseEntity<Map<String, Object>> deleteRow(
            @PathVariable String taskId,
            @RequestParam("rowIndex") int rowIndex) {
        Map<String, Object> result = new HashMap<>();
        try {
            ExcelTaskDTO task = previewService.deleteRow(taskId, rowIndex);
            result.put("success", true);
            result.put("data", task);
            result.put("message", "删除成功");
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/save/{taskId}")
    public ResponseEntity<Map<String, Object>> saveData(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> saveResult = previewService.saveData(taskId);
            result.putAll(saveResult);
        } catch (IllegalArgumentException | IllegalStateException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/clear/{taskId}")
    public ResponseEntity<Map<String, Object>> clearTask(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        try {
            previewService.clearTask(taskId);
            result.put("success", true);
            result.put("message", "任务已清除");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return ResponseEntity.ok(result);
    }
}
