package com.hackathon.evidence_bot_backend.util;

import org.apache.poi.ss.usermodel.*;

import java.io.File;

public class ExcelUtils {
    public static String extractText(String filePath) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (Workbook wb = WorkbookFactory.create(new File(filePath))) {
            for (Sheet sheet : wb) {
                for (Row row : sheet) {
                    boolean rowHasValue = false;
                    for (Cell cell : row) {
                        sb.append(cell.toString()).append("\t");
                        rowHasValue = true;
                    }
                    if (rowHasValue) sb.append("\n");
                }
            }
        }
        return sb.toString();
    }
}
