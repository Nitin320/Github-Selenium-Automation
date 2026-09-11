package reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import utils.ConfigReader;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ReportManager — Muhammad Sulthan K M
 *
 * Central controller for ExtentReports 5 across the entire test suite.
 *
 * Lifecycle:
 *   1. getInstance()   — lazily initialises the ExtentSparkReporter (HTML output)
 *   2. startTest()     — called in @BeforeEach / @BeforeAll — creates a named test node
 *   3. getTest()       — called from test steps or Hooks to log pass/fail/screenshot
 *   4. flushReports()  — called in @AfterAll — writes the HTML file to disk
 *
 * Thread safety: ThreadLocal<ExtentTest> ensures each parallel test gets its own node.
 * The root ExtentReports instance is shared and guarded by a synchronized initialiser.
 *
 * Report output: target/reports/ExtentReport_<timestamp>.html
 */
public class ReportManager {

    private static ExtentReports         extent;
    private static final ThreadLocal<ExtentTest> testThread = new ThreadLocal<>();

    /** Timestamp suffix so each run produces a unique file rather than overwriting the last */
    private static final String TIMESTAMP = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

    private ReportManager() {}

    // ── Initialisation ──────────────────────────────────────────────────────

    /**
     * Returns the singleton ExtentReports instance, creating it on first call.
     * Reads report directory from config.properties (key: reports.dir).
     */
    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            String dir        = ConfigReader.getProperty("reports.dir", "target/extent-reports");
            String reportPath = dir + "/ExtentReport_" + TIMESTAMP + ".html";
            new File(dir).mkdirs();

            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            spark.config().setTheme(Theme.DARK);
            spark.config().setDocumentTitle("Group 5 — GitHub Automation Report");
            spark.config().setReportName("GitHub Selenium Automation — QE Training");
            spark.config().setEncoding("UTF-8");
            spark.config().setTimeStampFormat("MMM dd, yyyy HH:mm:ss");

            extent = new ExtentReports();
            extent.attachReporter(spark);

            // System info appears in the report's "Environment" panel
            extent.setSystemInfo("Project",     "GitHub Selenium Automation");
            extent.setSystemInfo("Team",        "Group 5 — IBM QE Training");
            extent.setSystemInfo("Environment", "github.com (production)");
            extent.setSystemInfo("Browser",     ConfigReader.getProperty("browser", "chrome"));
            extent.setSystemInfo("Headless",    ConfigReader.getProperty("headless", "false"));
        }
        return extent;
    }

    // ── Test node management ─────────────────────────────────────────────────

    /** Create a new test node for this thread.  Call once per @Test method. */
    public static void startTest(String testName, String description) {
        ExtentTest test = getInstance().createTest(testName, description);
        testThread.set(test);
    }

    /** Return the ExtentTest node for the currently running test thread. */
    public static ExtentTest getTest() {
        return testThread.get();
    }

    // ── Suite teardown ───────────────────────────────────────────────────────

    /** Flush all pending test results to disk.  Must be called in @AfterAll. */
    public static void flushReports() {
        if (extent != null) {
            extent.flush();
        }
    }
}
