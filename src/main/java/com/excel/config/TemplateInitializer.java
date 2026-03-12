package com.excel.config;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class TemplateInitializer {

    @PostConstruct
    public void init() {
        try {
            createTemplatesDirectory();
            createUserTemplate();
            createOrgTemplate();
        } catch (IOException e) {
            throw new RuntimeException("初始化模板失败", e);
        }
    }

    private void createTemplatesDirectory() throws IOException {
        ClassPathResource resource = new ClassPathResource("config");
        try {
            File dir = resource.getFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (IOException e) {
            File dir = new File("src/main/resources/config");
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
    }

    private void createUserTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("config/user_template.xlsx");
        if (!resource.exists()) {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("用户数据");

            Row templateRow = sheet.createRow(0);
            templateRow.createCell(0).setCellValue("TEMPLATE_USER");

            Row headerRow = sheet.createRow(1);
            String[] headers = {"ID", "用户名", "真实姓名", "邮箱", "电话", "部门", "状态"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            Row exampleRow = sheet.createRow(2);
            String[] example = {"1", "zhangsan", "张三", "zhangsan@example.com", "13800138000", "技术部", "正常"};
            for (int i = 0; i < example.length; i++) {
                exampleRow.createCell(i).setCellValue(example[i]);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            File file = new File("src/main/resources/config/user_template.xlsx");
            file.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();
        }
    }

    private void createOrgTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("config/org_template.xlsx");
        if (!resource.exists()) {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("机构数据");

            Row templateRow = sheet.createRow(0);
            templateRow.createCell(0).setCellValue("TEMPLATE_ORG");

            Row headerRow = sheet.createRow(1);
            String[] headers = {"ID", "机构编码", "机构名称", "机构类型", "地址", "联系人", "联系电话", "状态"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            Row exampleRow = sheet.createRow(2);
            String[] example = {"1", "ORG001", "总公司", "总部", "北京市朝阳区", "李四", "13900139000", "正常"};
            for (int i = 0; i < example.length; i++) {
                exampleRow.createCell(i).setCellValue(example[i]);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            File file = new File("src/main/resources/config/org_template.xlsx");
            file.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();
        }
    }
}
