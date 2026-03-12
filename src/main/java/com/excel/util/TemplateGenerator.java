package com.excel.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

public class TemplateGenerator {

    public static void main(String[] args) throws IOException {
        generateUserTemplate();
        generateOrgTemplate();
        System.out.println("模板文件生成完成！");
    }

    private static void generateUserTemplate() throws IOException {
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

        Row exampleRow2 = sheet.createRow(3);
        String[] example2 = {"2", "lisi", "李四", "lisi@example.com", "13900139000", "市场部", "正常"};
        for (int i = 0; i < example2.length; i++) {
            exampleRow2.createCell(i).setCellValue(example2[i]);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        FileOutputStream fos = new FileOutputStream("src/main/resources/config/user_template.xlsx");
        workbook.write(fos);
        fos.close();
        workbook.close();
    }

    private static void generateOrgTemplate() throws IOException {
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

        Row exampleRow2 = sheet.createRow(3);
        String[] example2 = {"2", "ORG002", "分公司", "分部", "上海市浦东新区", "王五", "13700137000", "正常"};
        for (int i = 0; i < example2.length; i++) {
            exampleRow2.createCell(i).setCellValue(example2[i]);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        FileOutputStream fos = new FileOutputStream("src/main/resources/config/org_template.xlsx");
        workbook.write(fos);
        fos.close();
        workbook.close();
    }
}
