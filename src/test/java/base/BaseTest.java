package base;

import driver.DriverFactory;
import driver.DriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reporting.ReportManager;
import utils.ScreenshotUtils;

import java.nio.file.Path;

/**
 * BaseTest — JUnit 5 base class for all plain @Test classes.
 *
 * <p>Prints a console banner before and after every test method so the terminal
 * clearly shows which test is running, whether it passed or failed, and where to
 * find the failure screenshot when things go wrong.
 *
 * Author: Nitin
 */
public class BaseTest {

    private static final Logger LOG = LoggerFactory.getLogger(BaseTest.class);

    /** Convenience accessor for subclasses that need direct driver access. */
    protected WebDriver driver;

    @BeforeEach
    public void setUp(TestInfo info) {
        String testName = info.getDisplayName();
        String className = info.getTestClass().map(Class::getSimpleName).orElse("UnknownClass");
        LOG.info("\n╔══════════════════════════════════════════════════════════════╗\n"
               + "  🚀 TEST STARTING\n"
               + "  Class    : {}\n"
               + "  Method   : {}\n"
               + "╚══════════════════════════════════════════════════════════════╝",
               className, testName);

        DriverManager.setDriver(DriverFactory.createDriver());
        driver = DriverManager.getDriver();
        ReportManager.startTest(className + " — " + testName, "JUnit 5 direct test");
    }

    @AfterEach
    public void tearDown(TestInfo info) {
        String testName = info.getDisplayName();
        try {
            if (DriverManager.getDriver() != null) {
                Path shot = ScreenshotUtils.capture("teardown_" + testName);
                if (shot != null) {
                    LOG.info("  📸 Teardown screenshot  →  {}", shot.getFileName());
                }
            }
        } finally {
            DriverManager.quitDriver();
            ReportManager.flushReports();
        }
        LOG.info("\n╔══════════════════════════════════════════════════════════════╗\n"
               + "  ✅ TEST FINISHED : {}\n"
               + "╚══════════════════════════════════════════════════════════════╝",
               testName);
    }
}
