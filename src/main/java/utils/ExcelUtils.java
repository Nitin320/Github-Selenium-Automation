package utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ExcelUtils — reads test parameters from {@code .xlsx} files using Apache POI.
 *
 * <h3>Usage (TestNG / JUnit 5 DataProvider pattern)</h3>
 * <pre>{@code
 * Object[][] data = ExcelUtils.getSheetData(
 *     ConfigReader.getProperty("testdata.file"),
 *     "LoginData"
 * );
 * }</pre>
 *
 * <p>The <em>first row</em> of every sheet is treated as a header and is
 * excluded from the returned array.  Empty rows are skipped automatically.
 *
 * Author: Arsath
 */
public class ExcelUtils {

    private ExcelUtils() {
        // Utility class — no instantiation
    }

    // ------------------------------------------------------------------ //
    //  Public API                                                          //
    // ------------------------------------------------------------------ //

    /**
     * Reads all data rows (excluding the header) from the given sheet.
     *
     * @param filePath  path to the {@code .xlsx} file (relative to the
     *                  working directory or absolute)
     * @param sheetName name of the worksheet to read
     * @return 2-D {@code Object[][]} where each inner array represents one
     *         data row; column values are returned as {@link String}
     */
    public static Object[][] getSheetData(String filePath, String sheetName) {
        try (InputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException(
                    "Sheet '" + sheetName + "' not found in " + filePath);
            }

            int totalRows   = sheet.getLastRowNum(); // 0-based; row 0 = header
            int totalCols   = sheet.getRow(0).getLastCellNum();
            int dataRowCount = 0;

            // Count non-empty data rows (skip header at index 0)
            for (int r = 1; r <= totalRows; r++) {
                Row row = sheet.getRow(r);
                if (row != null && !isRowEmpty(row, totalCols)) {
                    dataRowCount++;
                }
            }

            Object[][] data = new Object[dataRowCount][totalCols];
            int dataIdx = 0;
            for (int r = 1; r <= totalRows; r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row, totalCols)) {
                    continue;
                }
                for (int c = 0; c < totalCols; c++) {
                    data[dataIdx][c] = getCellValueAsString(row.getCell(c));
                }
                dataIdx++;
            }
            return data;

        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to read Excel file: " + filePath, e);
        }
    }

    /**
     * Convenience overload that reads the sheet name configured in
     * {@code config.properties} under the key {@code testdata.sheet}.
     */
    public static Object[][] getSheetData(String filePath) {
        return getSheetData(filePath, ConfigReader.getProperty("testdata.sheet", "LoginData"));
    }

    /**
     * Same as {@link #getSheetData(String, String)} but includes the header row
     * at index 0 of the returned array.  Used internally by
     * {@link DataProviderUtils} to extract column names.
     *
     * <p>Row 0 = headers, rows 1..n = data rows.
     */
    static Object[][] getSheetDataWithHeader(String filePath, String sheetName) {
        try (InputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException(
                    "Sheet '" + sheetName + "' not found in " + filePath);
            }

            int totalRows = sheet.getLastRowNum();
            int totalCols = sheet.getRow(0).getLastCellNum();

            // Count non-empty rows including the header
            int rowCount = 0;
            for (int r = 0; r <= totalRows; r++) {
                Row row = sheet.getRow(r);
                if (row != null && !isRowEmpty(row, totalCols)) {
                    rowCount++;
                }
            }

            Object[][] data = new Object[rowCount][totalCols];
            int dataIdx = 0;
            for (int r = 0; r <= totalRows; r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row, totalCols)) {
                    continue;
                }
                for (int c = 0; c < totalCols; c++) {
                    data[dataIdx][c] = getCellValueAsString(row.getCell(c));
                }
                dataIdx++;
            }
            return data;

        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to read Excel file: " + filePath, e);
        }
    }

    // ------------------------------------------------------------------ //
    //  Internal helpers                                                    //
    // ------------------------------------------------------------------ //

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        CellType type = cell.getCellType() == CellType.FORMULA
            ? cell.getCachedFormulaResultType()
            : cell.getCellType();

        return switch (type) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                ? cell.getLocalDateTimeCellValue().toString()
                : formatNumeric(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case BLANK   -> "";
            default      -> "";
        };
    }

    /** Returns an integer string when the double has no fractional part. */
    private static String formatNumeric(double value) {
        return (value == Math.floor(value) && !Double.isInfinite(value))
            ? String.valueOf((long) value)
            : String.valueOf(value);
    }

    private static boolean isRowEmpty(Row row, int colCount) {
        for (int c = 0; c < colCount; c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && !cell.toString().trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
