package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DataProviderUtils — bridges {@link ExcelUtils} output with test frameworks.
 *
 * <p>This class sits on top of {@link ExcelUtils} and provides higher-level
 * helpers so that step definitions and test classes can consume Excel data
 * in a structured, named-column format rather than raw positional arrays.
 *
 * <h3>Usage — named column access (recommended)</h3>
 * <pre>{@code
 * List<Map<String, String>> rows =
 *     DataProviderUtils.getSheetAsMap("src/test/resources/testdata/github_testdata.xlsx", "LoginData");
 *
 * for (Map<String, String> row : rows) {
 *     String username = row.get("Username");
 *     String expected = row.get("ExpectedResult");
 * }
 * }</pre>
 *
 * <h3>Usage — JUnit 5 / raw Object[][] for @MethodSource</h3>
 * <pre>{@code
 * static Object[][] loginData() {
 *     return DataProviderUtils.getSheetData("LoginData");
 * }
 *
 * @ParameterizedTest
 * @MethodSource("loginData")
 * void testLogin(String testCaseId, String username, String password,
 *                String expectedResult, String notes) { ... }
 * }</pre>
 *
 * <h3>Usage — filter only "Pass" rows</h3>
 * <pre>{@code
 * List<Map<String, String>> happyPaths =
 *     DataProviderUtils.filterByColumn(rows, "ExpectedResult", "Pass");
 * }</pre>
 *
 * Author: Arsath
 */
public class DataProviderUtils {

    private DataProviderUtils() {
        // Utility class — no instantiation
    }

    // ------------------------------------------------------------------ //
    //  Core API                                                            //
    // ------------------------------------------------------------------ //

    /**
     * Reads a sheet and returns each data row as a {@code Map<columnHeader, cellValue>}.
     * The header row (row 0) becomes the map keys; subsequent rows become the values.
     *
     * @param filePath  path to the {@code .xlsx} file
     * @param sheetName worksheet name to read
     * @return ordered list of row maps; never {@code null}, may be empty
     */
    public static List<Map<String, String>> getSheetAsMap(String filePath, String sheetName) {
        // Obtain raw 2D array — row 0 is header, returned rows are data rows
        // We need the header separately, so we read with the internal raw form
        Object[][] raw = getRawWithHeader(filePath, sheetName);
        if (raw.length < 2) {
            return new ArrayList<>();
        }

        // Row 0 = headers
        int colCount = raw[0].length;
        String[] headers = new String[colCount];
        for (int c = 0; c < colCount; c++) {
            headers[c] = raw[0][c] == null ? "col" + c : raw[0][c].toString();
        }

        List<Map<String, String>> result = new ArrayList<>();
        for (int r = 1; r < raw.length; r++) {
            Map<String, String> row = new HashMap<>();
            for (int c = 0; c < colCount; c++) {
                Object val = (c < raw[r].length) ? raw[r][c] : null;
                row.put(headers[c], val == null ? "" : val.toString());
            }
            result.add(row);
        }
        return result;
    }

    /**
     * Convenience overload — resolves file path and sheet name from
     * {@code config.properties} ({@code testdata.file} and {@code testdata.sheet}).
     */
    public static List<Map<String, String>> getSheetAsMap(String sheetName) {
        return getSheetAsMap(
            ConfigReader.getProperty("testdata.file"),
            sheetName
        );
    }

    /**
     * Returns raw {@code Object[][]} (header row excluded) for use in
     * JUnit 5 {@code @MethodSource} or other data-provider patterns.
     * Delegates to {@link ExcelUtils#getSheetData(String, String)}.
     */
    public static Object[][] getSheetData(String filePath, String sheetName) {
        return ExcelUtils.getSheetData(filePath, sheetName);
    }

    /**
     * Convenience overload — reads the configured file and the specified sheet.
     */
    public static Object[][] getSheetData(String sheetName) {
        return ExcelUtils.getSheetData(
            ConfigReader.getProperty("testdata.file"),
            sheetName
        );
    }

    // ------------------------------------------------------------------ //
    //  Filter helpers                                                      //
    // ------------------------------------------------------------------ //

    /**
     * Returns only the rows where the given column equals the expected value
     * (case-insensitive comparison).
     *
     * <p>Example — get only positive test cases:
     * <pre>{@code
     * List<Map<String, String>> passCases =
     *     DataProviderUtils.filterByColumn(allRows, "ExpectedResult", "Pass");
     * }</pre>
     */
    public static List<Map<String, String>> filterByColumn(
            List<Map<String, String>> rows, String column, String expectedValue) {
        List<Map<String, String>> filtered = new ArrayList<>();
        for (Map<String, String> row : rows) {
            String actual = row.getOrDefault(column, "");
            if (actual.equalsIgnoreCase(expectedValue)) {
                filtered.add(row);
            }
        }
        return filtered;
    }

    // ------------------------------------------------------------------ //
    //  Internal helpers                                                    //
    // ------------------------------------------------------------------ //

    /**
     * Returns the raw sheet content INCLUDING the header row at index 0.
     * This is needed internally to extract column names.
     */
    private static Object[][] getRawWithHeader(String filePath, String sheetName) {
        // ExcelUtils skips the header, so we use a direct read that keeps it.
        // We reuse ExcelUtils for the data rows, and prepend the header ourselves
        // by reading the workbook via a thin wrapper in ExcelUtils.
        return ExcelUtils.getSheetDataWithHeader(filePath, sheetName);
    }
}
