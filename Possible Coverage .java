import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public void processExcelValidations(String filePath) {
    FileInputStream fis = null;
    Workbook workbook = null;
    try {
        fis = new FileInputStream(filePath);
        workbook = WorkbookFactory.create(fis);

        Sheet sheet = workbook.getSheetAt(0);  // Assuming first sheet

        // Create header row if missing
        Row header = sheet.getRow(0);
        if (header == null) {
            header = sheet.createRow(0);
        }

        int colPossibleValid = 0; // Column 1 (POSSIBLE_VALIDATIONS)
        int colScenarioInfo = 1;  // Column 2 (SCENARIOS_INFO)
        int colCoverage = 2;      // Column 3 (POSSIBLE_COVERAGE)

        if (header.getCell(colCoverage) == null) {
            header.createCell(colCoverage).setCellValue("POSSIBLE_COVERAGE");
        }

        int lastRow = sheet.getLastRowNum();
        for (int i = 1; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String possibleVal = getCellValue(row.getCell(colPossibleValid));
            String scenarioInfo = getCellValue(row.getCell(colScenarioInfo));

            String output = "";

            // CONDITION 1:
            if ("SENT_TO_GATEWAY".equalsIgnoreCase(scenarioInfo)
                    || "SENT_TO_PROCESSOR".equalsIgnoreCase(scenarioInfo)) {

                StringBuilder sb = new StringBuilder();
                String[] statuses = possibleVal.split(",");
                for (String status : statuses) {
                    if (status == null) continue;
                    status = status.trim();
                    if (status.endsWith("FAIL") || status.endsWith("NACK")) {
                        sb.append(status).append("\n");
                    }
                }
                output = sb.toString().trim();
            }

            // CONDITION 2:
            else if (scenarioInfo.matches(".*APLHeaders_Neg.*|.*Neg_APLHeaders.*|.*Negative_APLHeaders_Neg.*")) {

                StringBuilder sb = new StringBuilder();
                String[] statuses = possibleVal.split(",");
                for (String status : statuses) {
                    if (status == null) continue;
                    status = status.trim();
                    if (!"PREVALIDATION_FAIL".equalsIgnoreCase(status)) {
                        sb.append(status).append("\n");
                    }
                }
                output = sb.toString().trim();
            }

            // CONDITION 3:
            else if (scenarioInfo.contains("Duplicate_Neg") || scenarioInfo.contains("Neg_Duplicate")) {

                StringBuilder sb = new StringBuilder();
                String[] statuses = possibleVal.split(",");
                for (String status : statuses) {
                    if (status == null) continue;
                    status = status.trim();
                    if (!"DUPCHECK_FAIL".equalsIgnoreCase(status)) {
                        sb.append(status).append("\n");
                    }
                }
                output = sb.toString().trim();
            }

            // Write into Coverage cell (create or overwrite)
            if (row.getCell(colCoverage) == null) {
                row.createCell(colCoverage).setCellValue(output);
            } else {
                row.getCell(colCoverage).setCellValue(output);
            }
        }

        // Save file (overwrite)
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            workbook.write(fos);
        } finally {
            if (fos != null) {
                try { fos.close(); } catch (Exception ignore) {}
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        if (workbook != null) {
            try { workbook.close(); } catch (Exception ignore) {}
        }
        if (fis != null) {
            try { fis.close(); } catch (Exception ignore) {}
        }
    }
}

/**
 * Java 8 compatible helper to read a cell value as String.
 * Preserves formatted numeric values and formats dates as yyyy-MM-dd.
 */
private String getCellValue(Cell cell) {
    if (cell == null) return "";

    DataFormatter formatter = new DataFormatter();
    try {
        CellType type = cell.getCellType();

        switch (type) {
            case STRING:
                return cell.getStringCellValue();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    return sdf.format(date);
                } else {
                    // preserves formatting like "1,234.56" or "0.00"
                    return formatter.formatCellValue(cell);
                }

            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());

            case FORMULA:
                // If you want to evaluate formulas uncomment below lines and provide evaluator
                // FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                // return formatter.formatCellValue(cell, evaluator);
                return formatter.formatCellValue(cell); // cached/formatted value

            case BLANK:
                return "";

            case ERROR:
            default:
                return "";
        }
    } catch (Exception e) {
        return "";
    }
}
