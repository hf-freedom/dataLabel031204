package com.excel.config;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class ExcelTemplateConfig {

    public static final String TEMPLATE_DIR = "templates";
    public static final String USER_TEMPLATE = "user_template.xlsx";
    public static final String ORG_TEMPLATE = "org_template.xlsx";
    public static final String USER_TEMPLATE_ID = "USER_001";
    public static final String ORG_TEMPLATE_ID = "ORG_001";

    @PostConstruct
    public void init() throws IOException {
        Path path = Paths.get("src/main/resources", TEMPLATE_DIR);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        createUserTemplate();
        createOrgTemplate();
    }

    private void createUserTemplate() throws IOException {
        Path filePath = Paths.get("src/main/resources", TEMPLATE_DIR, USER_TEMPLATE);
        if (Files.exists(filePath)) {
            return;
        }
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("用户信息");
            Row templateIdRow = sheet.createRow(0);
            templateIdRow.createCell(0).setCellValue(USER_TEMPLATE_ID);
            
            Row headerRow = sheet.createRow(1);
            headerRow.createCell(0).setCellValue("用户名");
            headerRow.createCell(1).setCellValue("年龄");
            headerRow.createCell(2).setCellValue("邮箱");
            headerRow.createCell(3).setCellValue("手机号");
            
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                workbook.write(fos);
            }
        }
    }

    private void createOrgTemplate() throws IOException {
        Path filePath = Paths.get("src/main/resources", TEMPLATE_DIR, ORG_TEMPLATE);
        if (Files.exists(filePath)) {
            return;
        }
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("机构信息");
            Row templateIdRow = sheet.createRow(0);
            templateIdRow.createCell(0).setCellValue(ORG_TEMPLATE_ID);
            
            Row headerRow = sheet.createRow(1);
            headerRow.createCell(0).setCellValue("机构名称");
            headerRow.createCell(1).setCellValue("机构代码");
            headerRow.createCell(2).setCellValue("联系人");
            headerRow.createCell(3).setCellValue("联系电话");
            
            try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                workbook.write(fos);
            }
        }
    }
}
