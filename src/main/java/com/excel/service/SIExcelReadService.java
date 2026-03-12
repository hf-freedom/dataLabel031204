package com.excel.service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SIExcelReadService {

    public String readTemplateId(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row templateIdRow = sheet.getRow(0);
            if (templateIdRow != null) {
                Cell cell = templateIdRow.getCell(0);
                if (cell != null) {
                    return getCellValue(cell);
                }
            }
            return null;
        }
    }

    public List<String> readHeaders(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(1);
            List<String> headers = new ArrayList<>();
            if (headerRow != null) {
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell cell = headerRow.getCell(i);
                    headers.add(cell != null ? getCellValue(cell) : "");
                }
            }
            return headers;
        }
    }

    public List<Map<String, Object>> readData(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<String> headers = readHeaders(file);
            List<Map<String, Object>> dataList = new ArrayList<>();
            
            for (int rowNum = 2; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) continue;
                
                Map<String, Object> dataMap = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i);
                    dataMap.put(headers.get(i), getCellValue(cell));
                }
                if (!isRowEmpty(dataMap)) {
                    dataList.add(dataMap);
                }
            }
            return dataList;
        }
    }

    private boolean isRowEmpty(Map<String, Object> row) {
        for (Object value : row.values()) {
            if (value != null && !value.toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
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
}
