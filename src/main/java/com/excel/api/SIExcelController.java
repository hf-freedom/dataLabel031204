package com.excel.api;

import com.excel.dto.Result;
import com.excel.service.SIExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/excel")
public class SIExcelController {

    @Autowired
    private SIExcelService excelService;

    @GetMapping("/templates")
    public Result<Map<String, String>> getTemplates() {
        return Result.success(excelService.getTemplateList());
    }

    @GetMapping("/template/download/{templateId}")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable String templateId) {
        try {
            byte[] data = excelService.downloadTemplate(templateId);
            String fileName = excelService.getTemplateFileName(templateId);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = excelService.parseExcel(file);
            return Result.success("上传成功", result);
        } catch (IOException e) {
            return Result.error("文件解析失败: " + e.getMessage());
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/preview/{taskId}")
    public Result<Map<String, Object>> previewData(@PathVariable String taskId) {
        try {
            Map<String, Object> result = excelService.getPreviewData(taskId);
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/row/{taskId}/{rowIndex}")
    public Result<Void> deleteRow(@PathVariable String taskId, @PathVariable Integer rowIndex) {
        try {
            excelService.deleteRow(taskId, rowIndex);
            return Result.success("删除成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/row/{taskId}/{rowIndex}")
    public Result<Void> updateRow(@PathVariable String taskId, 
                                   @PathVariable Integer rowIndex,
                                   @RequestBody Object rowData) {
        try {
            excelService.updateRow(taskId, rowIndex, rowData);
            return Result.success("更新成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/save/{taskId}")
    public Result<List<Object>> saveData(@PathVariable String taskId) {
        try {
            List<Object> data = excelService.saveData(taskId);
            return Result.success("保存成功", data);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
