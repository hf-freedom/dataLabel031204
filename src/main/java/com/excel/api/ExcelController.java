package com.excel.api;

import com.excel.dto.ExcelDataVo;
import com.excel.service.SIExcelCacheService;
import com.excel.service.SIExcelReadService;
import com.excel.service.SIExcelTemplateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/excel")
@Api(tags = "Excel上传预览接口")
@CrossOrigin
public class ExcelController {

    private final SIExcelTemplateService templateService;
    private final SIExcelReadService excelReadService;
    private final SIExcelCacheService excelCacheService;

    public ExcelController(SIExcelTemplateService templateService,
                           SIExcelReadService excelReadService,
                           SIExcelCacheService excelCacheService) {
        this.templateService = templateService;
        this.excelReadService = excelReadService;
        this.excelCacheService = excelCacheService;
    }

    @GetMapping("/template/user")
    @ApiOperation("下载用户模板")
    public ResponseEntity<FileSystemResource> downloadUserTemplate() throws Exception {
        File file = templateService.getUserTemplate();
        return downloadFile(file, "user_template.xlsx");
    }

    @GetMapping("/template/org")
    @ApiOperation("下载机构模板")
    public ResponseEntity<FileSystemResource> downloadOrgTemplate() throws Exception {
        File file = templateService.getOrgTemplate();
        return downloadFile(file, "org_template.xlsx");
    }

    @PostMapping("/upload")
    @ApiOperation("上传Excel文件")
    public ResponseEntity<ExcelDataVo> uploadExcel(
            @ApiParam("Excel文件") @RequestParam("file") MultipartFile file) throws Exception {
        String templateId = excelReadService.readTemplateId(file);
        List<String> headers = excelReadService.readHeaders(file);
        List<Map<String, Object>> dataList = excelReadService.readData(file);
        
        String taskId = UUID.randomUUID().toString();
        excelCacheService.saveExcelData(taskId, templateId, headers, dataList);
        
        ExcelDataVo vo = new ExcelDataVo();
        vo.setTaskId(taskId);
        vo.setTemplateId(templateId);
        vo.setHeaders(headers);
        vo.setDataList(dataList);
        
        return ResponseEntity.ok(vo);
    }

    @GetMapping("/preview/{taskId}")
    @ApiOperation("预览Excel数据")
    public ResponseEntity<ExcelDataVo> previewData(
            @ApiParam("任务ID") @PathVariable String taskId) {
        List<Map<String, Object>> dataList = excelCacheService.getExcelData(taskId);
        String templateId = excelCacheService.getTemplateId(taskId);
        List<String> headers = excelCacheService.getHeaders(taskId);
        
        ExcelDataVo vo = new ExcelDataVo();
        vo.setTaskId(taskId);
        vo.setTemplateId(templateId);
        vo.setHeaders(headers);
        vo.setDataList(dataList);
        
        return ResponseEntity.ok(vo);
    }

    @DeleteMapping("/data/{taskId}/{index}")
    @ApiOperation("删除指定行数据")
    public ResponseEntity<Void> deleteData(
            @ApiParam("任务ID") @PathVariable String taskId,
            @ApiParam("行索引") @PathVariable int index) {
        excelCacheService.removeData(taskId, index);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/data/{taskId}/{index}")
    @ApiOperation("修改指定行数据")
    public ResponseEntity<Void> updateData(
            @ApiParam("任务ID") @PathVariable String taskId,
            @ApiParam("行索引") @PathVariable int index,
            @RequestBody Map<String, Object> newData) {
        excelCacheService.updateData(taskId, index, newData);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/save/{taskId}")
    @ApiOperation("保存并清除临时数据")
    public ResponseEntity<List<Map<String, Object>>> saveData(
            @ApiParam("任务ID") @PathVariable String taskId) {
        List<Map<String, Object>> result = excelCacheService.saveAndClear(taskId);
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<FileSystemResource> downloadFile(File file, String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        FileSystemResource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .body(resource);
    }
}
