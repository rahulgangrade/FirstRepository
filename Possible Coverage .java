public void processExcelValidations(String filePath) {
    try (FileInputStream fis = new FileInputStream(filePath);
         Workbook workbook = WorkbookFactory.create(fis)) {

        Sheet sheet = workbook.getSheetAt(0);  // Assuming first sheet

        // Create new 3rd column "POSSIBLE_COVERAGE" if not present
        Row header = sheet.getRow(0);
        int colPossibleValid = 0; // Column 1
        int colScenarioInfo = 1;  // Column 2
        int colCoverage = 2;      // Column 3

        if (header.getCell(colCoverage) == null) {
            header.createCell(colCoverage).setCellValue("POSSIBLE_COVERAGE");
        }

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);
            if (row == null) continue;

            String possibleVal = getCellValue(row.getCell(colPossibleValid));
            String scenarioInfo = getCellValue(row.getCell(colScenarioInfo));

            String output = "";

            // ---------------------------------------------------
            // CONDITION 1:
            // If scenarioInfo is SENT_TO_GATEWAY or SENT_TO_PROCESSOR
            // Then pick all statuses from possibleVal where value ends with FAIL or NACK
            // ---------------------------------------------------
            if ("SENT_TO_GATEWAY".equalsIgnoreCase(scenarioInfo)
                    || "SENT_TO_PROCESSOR".equalsIgnoreCase(scenarioInfo)) {

                StringBuilder sb = new StringBuilder();
                String[] statuses = possibleVal.split(",");

                for (String status : statuses) {
                    status = status.trim();
                    if (status.endsWith("FAIL") || status.endsWith("NACK")) {
                        sb.append(status).append("\n");
                    }
                }
                output = sb.toString().trim();
            }

            // ---------------------------------------------------
            // CONDITION 2:
            // If scenarioInfo contains APLHeaders_Neg, Neg_APLHeaders,
            // Negative_APLHeaders_Neg → Do NOT include PREVALIDATION_FAIL
            // ---------------------------------------------------
            else if (scenarioInfo.matches(".*APLHeaders_Neg.*|.*Neg_APLHeaders.*|.*Negative_APLHeaders_Neg.*")) {

                StringBuilder sb = new StringBuilder();
                String[] statuses = possibleVal.split(",");

                for (String status : statuses) {
                    status = status.trim();
                    if (!"PREVALIDATION_FAIL".equalsIgnoreCase(status)) {
                        sb.append(status).append("\n");
                    }
                }
                output = sb.toString().trim();
            }

            // ---------------------------------------------------
            // CONDITION 3:
            // If scenarioInfo contains Duplicate_Neg or Neg_Duplicate
            // then DON'T pick DUPCHECK_FAIL
            // ---------------------------------------------------
            else if (scenarioInfo.contains("Duplicate_Neg") || scenarioInfo.contains("Neg_Duplicate")) {

                StringBuilder sb = new StringBuilder();
                String[] statuses = possibleVal.split(",");

                for (String status : statuses) {
                    status = status.trim();
                    if (!"DUPCHECK_FAIL".equalsIgnoreCase(status)) {
                        sb.append(status).append("\n");
                    }
                }
                output = sb.toString().trim();
            }

            // Write into Coverage cell
            row.createCell(colCoverage).setCellValue(output);
        }

        // Save file
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

// Helper to safely read cell string
private String getCellValue(Cell cell) {
    if (cell == null) return "";
    return switch (cell.getCellType()) {
        case STRING -> cell.getStringCellValue();
        case NUMERIC -> String.valueOf(cell.getNumericCellValue());
        case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
        default -> "";
    };
}
