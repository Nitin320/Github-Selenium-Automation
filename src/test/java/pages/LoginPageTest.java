package pages;

import driver.DriverFactory;
import driver.DriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LoginPageTest — smoke test that the LoginPage page object can open GitHub.
 * Lives in src/test/java/pages/ and is picked up by the JUnit Platform
 * directly (not via Cucumber / TestSuiteRunner).
 *
 * NOTE: intentionally not extending BaseTest because it lives in a different
 * package and was authored separately. Driver lifecycle is managed manually
 * here so the class remains self-contained.
 */
class LoginPageTest {

    private LoginPage loginPage;

    @BeforeEach
    void setUp() {
        DriverManager.setDriver(DriverFactory.createDriver());
        loginPage = new LoginPage();
    }

    @AfterEach
    void tearDown() {
        DriverManager.quitDriver();
    }

    @Test
    void shouldLoginFromDotenv() {
        loginPage.open().loginFromConfig();
        assertTrue(DriverManager.getDriver().getCurrentUrl().contains("github.com"));
    }
}
