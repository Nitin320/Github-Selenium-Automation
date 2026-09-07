package pages;

import driver.DriverFactory;
import driver.DriverManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginPageTest {

    private LoginPage loginPage;

    @BeforeEach
    void setUp() {
        DriverManager.setDriver(DriverFactory.createDriver());
        loginPage = new LoginPage();
    }

    @Test
    void shouldLoginFromDotenv() {
        loginPage.open().loginFromConfig();
        assertTrue(DriverManager.getDriver().getCurrentUrl().contains("github.com"));
    }
}
