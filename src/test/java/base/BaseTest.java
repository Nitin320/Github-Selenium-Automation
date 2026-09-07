package base;

import driver.DriverFactory;
import driver.DriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import reporting.ReportManager;
import utils.ScreenshotUtils;

/**
 * BaseTest — JUnit 5 base class for all test classes.
 * Author: Nitin
 */
public class BaseTest {

    /** Convenience accessor for subclasses that need direct driver access. */
    protected WebDriver driver;

    @BeforeEach
    public void setUp() {
        DriverManager.setDriver(DriverFactory.createDriver());
        driver = DriverManager.getDriver();
    }

    @AfterEach
    public void tearDown() {
        try {
            if (DriverManager.getDriver() != null) {
                ScreenshotUtils.capture("teardown");
            }
        } finally {
            DriverManager.quitDriver();
            ReportManager.flush();
        }
    }
}
