import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/**
 * Standalone script (no external deps) that creates github_testdata.xlsx.
 * Run from the project root:
 *   java Github-Selenium-Automation/src/test/resources/testdata/GenerateTestData.java
 *
 * The .xlsx format is an Open XML ZIP archive; we build the minimal
 * required parts ([Content_Types].xml, workbook.xml, sheet XML files)
 * by hand so no Apache POI is needed at generation time.
 *
 * Author: Arsath
 */
public class GenerateTestData {

    // ------------------------------------------------------------------ //
    //  Sheet definitions                                                   //
    // ------------------------------------------------------------------ //
    record Sheet(String name, String[] headers, String[][] rows) {}

    static final Sheet[] SHEETS = {

        new Sheet("LoginData",
            new String[]{"TestCaseId","Username","Password","ExpectedResult","Notes"},
            new String[][]{
                {"TC_LOGIN_001","valid_user",  "","Pass","Standard valid login — credentials from env variable"},
                {"TC_LOGIN_002","invalid_user","","Fail","Wrong username — expect error message"},
                {"TC_LOGIN_003","valid_user",  "","Fail","Empty password — expect validation error"},
            }
        ),

        new Sheet("RepoData",
            new String[]{"TestCaseId","RepositoryName","Description","Visibility","InitWithReadme","ExpectedResult","Notes"},
            new String[][]{
                {"TC_REPO_001","selenium-repo-001",  "Automated Selenium test repository",  "Public", "true", "Pass","Create public repo with README"},
                {"TC_REPO_002","automation-repo-002","Private repo for automation testing", "Private","false","Pass","Create private repo without README"},
                {"TC_REPO_003","selenium-repo-001",  "Duplicate repo name",                 "Public", "true", "Fail","Duplicate name should fail"},
                {"TC_REPO_004","",                   "Repo with empty name",                "Public", "false","Fail","Empty name should fail validation"},
            }
        ),

        new Sheet("IssueData",
            new String[]{"TestCaseId","RepositoryName","IssueTitle","IssueBody","Labels","ExpectedResult","Notes"},
            new String[][]{
                {"TC_ISSUE_001","Hello-World","Bug in login flow",            "Steps: 1. Navigate  2. Enter creds  3. Click submit","bug",          "Pass","Create issue with bug label"},
                {"TC_ISSUE_002","Hello-World","Feature: dark mode support",   "As a user I want dark mode",                         "enhancement",  "Pass","Create feature request issue"},
                {"TC_ISSUE_003","Hello-World","Improve docs for API section", "The API docs are missing examples",                  "documentation","Pass","Documentation issue"},
                {"TC_ISSUE_004","Hello-World","",                             "Issue without a title",                              "",             "Fail","Empty title should fail validation"},
            }
        ),

        new Sheet("GistData",
            new String[]{"TestCaseId","GistDescription","FileName","FileContent","Visibility","ExpectedResult","Notes"},
            new String[][]{
                {"TC_GIST_001","Hello world gist","hello.py",      "print(\"Hello, World!\")","Public","Pass","Create public gist"},
                {"TC_GIST_002","Python snippet",  "utils.py",      "def add(a, b): return a + b","Secret","Pass","Create secret gist"},
                {"TC_GIST_003","",                "nodesc.txt",    "Content without description","Public","Pass","Gist without description is valid"},
                {"TC_GIST_004","No content gist", "empty.txt",     "","Public","Fail","Empty file content should fail"},
            }
        ),

        new Sheet("SearchData",
            new String[]{"TestCaseId","SearchQuery","FilterType","ExpectedMinResults","ExpectedResult","Notes"},
            new String[][]{
                {"TC_SEARCH_001","selenium",               "Repositories","1","Pass","Global repo search"},
                {"TC_SEARCH_002","automation framework",   "Repositories","1","Pass","Multi-word search"},
                {"TC_SEARCH_003","xyzxyz_nonexistent_123", "Repositories","0","Pass","No results for nonsense query"},
                {"TC_SEARCH_004","",                       "Repositories","0","Fail","Empty search should not submit"},
            }
        ),
    };

    // ------------------------------------------------------------------ //
    //  Entry point                                                         //
    // ------------------------------------------------------------------ //
    public static void main(String[] args) throws Exception {
        Path out = Path.of(
            "Github-Selenium-Automation/src/test/resources/testdata/github_testdata.xlsx");
        Files.createDirectories(out.getParent());

        // Shared-strings table — collect all unique cell values in encounter order
        List<String> sharedStrings = new ArrayList<>();
        Map<String, Integer> ssIndex = new LinkedHashMap<>();

        // Pre-scan all text so we can build the shared strings part
        for (Sheet s : SHEETS) {
            for (String h : s.headers()) index(h, sharedStrings, ssIndex);
            for (String[] row : s.rows())
                for (String cell : row) index(cell, sharedStrings, ssIndex);
        }

        // Build ZIP in memory
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(baos)) {
            zip.setLevel(Deflater.BEST_COMPRESSION);

            addEntry(zip, "[Content_Types].xml", contentTypes(SHEETS.length));
            addEntry(zip, "_rels/.rels",          rootRels());
            addEntry(zip, "xl/workbook.xml",      workbook(SHEETS));
            addEntry(zip, "xl/_rels/workbook.xml.rels", workbookRels(SHEETS.length));
            addEntry(zip, "xl/sharedStrings.xml", sharedStringsXml(sharedStrings));
            addEntry(zip, "xl/styles.xml",        styles());

            for (int i = 0; i < SHEETS.length; i++) {
                addEntry(zip, "xl/worksheets/sheet" + (i + 1) + ".xml",
                         sheetXml(SHEETS[i], ssIndex));
            }
        }

        Files.write(out, baos.toByteArray());
        System.out.println("Created: " + out.toAbsolutePath());
        System.out.printf("  Sheets: %s%n",
            Arrays.stream(SHEETS).map(Sheet::name)
                .reduce((a, b) -> a + ", " + b).orElse(""));
    }

    // ------------------------------------------------------------------ //
    //  XML builders                                                        //
    // ------------------------------------------------------------------ //

    static String contentTypes(int sheetCount) {
        StringBuilder sb = new StringBuilder(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
            "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
            "<Default Extension=\"xml\"  ContentType=\"application/xml\"/>" +
            "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>" +
            "<Override PartName=\"/xl/sharedStrings.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml\"/>" +
            "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
        );
        for (int i = 1; i <= sheetCount; i++)
            sb.append("<Override PartName=\"/xl/worksheets/sheet").append(i)
              .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
        sb.append("</Types>");
        return sb.toString();
    }

    static String rootRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
            "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>" +
            "</Relationships>";
    }

    static String workbook(Sheet[] sheets) {
        StringBuilder sb = new StringBuilder(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" " +
            "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
            "<sheets>"
        );
        for (int i = 0; i < sheets.length; i++)
            sb.append("<sheet name=\"").append(escXml(sheets[i].name()))
              .append("\" sheetId=\"").append(i + 1)
              .append("\" r:id=\"rId").append(i + 1).append("\"/>");
        sb.append("</sheets></workbook>");
        return sb.toString();
    }

    static String workbookRels(int sheetCount) {
        StringBuilder sb = new StringBuilder(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
            "<Relationship Id=\"rIdSS\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings\" Target=\"sharedStrings.xml\"/>" +
            "<Relationship Id=\"rIdST\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
        );
        for (int i = 1; i <= sheetCount; i++)
            sb.append("<Relationship Id=\"rId").append(i)
              .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet")
              .append(i).append(".xml\"/>");
        sb.append("</Relationships>");
        return sb.toString();
    }

    static String sharedStringsXml(List<String> strings) {
        StringBuilder sb = new StringBuilder(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" count=\"" +
            strings.size() + "\" uniqueCount=\"" + strings.size() + "\">"
        );
        for (String s : strings)
            sb.append("<si><t xml:space=\"preserve\">").append(escXml(s)).append("</t></si>");
        sb.append("</sst>");
        return sb.toString();
    }

    static String styles() {
        // Minimal styles: index 0 = normal, index 1 = bold (header)
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" +
            "<fonts count=\"2\">" +
            "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>" +
            "<font><b/><sz val=\"11\"/><name val=\"Calibri\"/><color rgb=\"FFFFFFFF\"/></font>" +
            "</fonts>" +
            "<fills count=\"3\">" +
            "<fill><patternFill patternType=\"none\"/></fill>" +
            "<fill><patternFill patternType=\"gray125\"/></fill>" +
            "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF1F4E79\"/></patternFill></fill>" +
            "</fills>" +
            "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>" +
            "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>" +
            "<cellXfs count=\"2\">" +
            "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>" +
            "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\"/>" +
            "</cellXfs>" +
            "</styleSheet>";
    }

    static String sheetXml(Sheet sheet, Map<String, Integer> ssIndex) {
        StringBuilder sb = new StringBuilder(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" +
            "<sheetData>"
        );
        // Header row (row 1, styleIndex=1 for bold+blue)
        sb.append("<row r=\"1\">");
        for (int c = 0; c < sheet.headers().length; c++) {
            String addr = colLetter(c) + "1";
            sb.append("<c r=\"").append(addr).append("\" t=\"s\" s=\"1\">")
              .append("<v>").append(ssIndex.get(sheet.headers()[c])).append("</v></c>");
        }
        sb.append("</row>");
        // Data rows
        for (int r = 0; r < sheet.rows().length; r++) {
            int rowNum = r + 2;
            sb.append("<row r=\"").append(rowNum).append("\">");
            String[] row = sheet.rows()[r];
            for (int c = 0; c < row.length; c++) {
                String addr = colLetter(c) + rowNum;
                sb.append("<c r=\"").append(addr).append("\" t=\"s\">")
                  .append("<v>").append(ssIndex.get(row[c])).append("</v></c>");
            }
            sb.append("</row>");
        }
        sb.append("</sheetData></worksheet>");
        return sb.toString();
    }

    // ------------------------------------------------------------------ //
    //  Utilities                                                           //
    // ------------------------------------------------------------------ //

    static void index(String value, List<String> list, Map<String, Integer> map) {
        if (!map.containsKey(value)) {
            map.put(value, list.size());
            list.add(value);
        }
    }

    static void addEntry(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    static String colLetter(int index) {
        StringBuilder sb = new StringBuilder();
        index++;
        while (index > 0) {
            index--;
            sb.insert(0, (char) ('A' + index % 26));
            index /= 26;
        }
        return sb.toString();
    }

    static String escXml(String s) {
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&apos;");
    }
}
