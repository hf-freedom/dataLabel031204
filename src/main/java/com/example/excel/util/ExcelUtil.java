package com.example.excel.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

@Slf4j
public class ExcelUtil {

    public static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double numericValue = cell.getNumericCellValue();
                if (numericValue == (long) numericValue) {
                    return String.valueOf((long) numericValue);
                }
                return String.valueOf(numericValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    public static Workbook createTemplateWorkbook(String templateId, String templateName, 
                                                   List<String> headers, Map<String, String> fieldMapping) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("数据模板");

        // 第一行：模板ID（隐藏行，系统识别用）
        Row idRow = sheet.createRow(0);
        Cell idCell = idRow.createCell(0);
        idCell.setCellValue("TEMPLATE_ID:" + templateId);
        idCell.setCellStyle(createHiddenStyle(workbook));

        // 第二行：中文表头（必填项标记*）
        CellStyle headerStyle = createHeaderStyle(workbook);
        Row headerRow = sheet.createRow(1);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 22);
        }

        // 第三行：英文属性名（对应实体类字段）
        CellStyle fieldStyle = createFieldStyle(workbook);
        Row fieldRow = sheet.createRow(2);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = fieldRow.createCell(i);
            String header = headers.get(i);
            String fieldName = fieldMapping.getOrDefault(header, "");
            cell.setCellValue(fieldName);
            cell.setCellStyle(fieldStyle);
        }

        // 第四行：填写示例
        CellStyle exampleStyle = createExampleStyle(workbook);
        Row exampleRow = sheet.createRow(3);
        String[] examples = getExamplesByTemplateId(templateId, headers.size());
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = exampleRow.createCell(i);
            cell.setCellValue(examples[i]);
            cell.setCellStyle(exampleStyle);
        }

        // 第五行开始：用户填写区域
        CellStyle dataStyle = createDataStyle(workbook);
        for (int row = 4; row <= 8; row++) {
            Row dataRow = sheet.createRow(row);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = dataRow.createCell(i);
                cell.setCellStyle(dataStyle);
            }
        }

        // 添加数据有效性提示
        addDataValidation(sheet, headers, fieldMapping);

        return workbook;
    }

    private static String[] getExamplesByTemplateId(String templateId, int size) {
        if (templateId.contains("USER")) {
            return new String[]{"zhangsan", "张三", "13800138000", "zhangsan@example.com", "技术部", "启用", "备注信息"};
        } else if (templateId.contains("ORG")) {
            return new String[]{"ORG001", "北京分公司", "ORG000", "分公司", "13800138001", "李四", "北京市朝阳区", "启用", "备注"};
        }
        String[] examples = new String[size];
        for (int i = 0; i < size; i++) {
            examples[i] = "示例" + (i + 1);
        }
        return examples;
    }

    private static void addDataValidation(Sheet sheet, List<String> headers, Map<String, String> fieldMapping) {
        // 可以在这里添加数据有效性验证，如手机号格式、下拉列表等
    }

    private static CellStyle createHiddenStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        font.setItalic(true);
        style.setFont(font);
        return style;
    }

    private static CellStyle createFieldStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.BLUE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createExampleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setItalic(true);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    public static void downloadTemplate(HttpServletResponse response, String fileName, Workbook workbook) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName);

        try (OutputStream out = response.getOutputStream()) {
            workbook.write(out);
            out.flush();
        }
    }

    public static String readTemplateId(InputStream inputStream) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row firstRow = sheet.getRow(0);
            if (firstRow != null) {
                Cell firstCell = firstRow.getCell(0);
                if (firstCell != null) {
                    String value = getCellValue(firstCell);
                    if (value.startsWith("TEMPLATE_ID:")) {
                        return value.substring("TEMPLATE_ID:".length());
                    }
                }
            }
        }
        return null;
    }

    public static List<Map<String, Object>> readExcelData(InputStream inputStream, Map<String, String> fieldMapping) throws IOException {
        List<Map<String, Object>> dataList = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            // 第2行是中文表头
            Row headerRow = sheet.getRow(1);
            if (headerRow == null) {
                return dataList;
            }

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValue(cell));
            }

            // 从第5行开始读取数据（跳过模板ID行、中文表头行、英文属性行、示例行）
            for (int i = 4; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                Map<String, Object> rowData = new LinkedHashMap<>();
                rowData.put("_rowNum", i + 1);

                for (int j = 0; j < headers.size(); j++) {
                    String header = headers.get(j);
                    String fieldName = fieldMapping.getOrDefault(header, header);
                    Cell cell = row.getCell(j);
                    String value = getCellValue(cell);
                    rowData.put(fieldName, value);
                }

                dataList.add(rowData);
            }
        }

        return dataList;
    }

    private static boolean isEmptyRow(Row row) {
        for (Cell cell : row) {
            String value = getCellValue(cell);
            if (!value.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static byte[] workbookToBytes(Workbook workbook) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            workbook.write(baos);
            return baos.toByteArray();
        }
    }
}
