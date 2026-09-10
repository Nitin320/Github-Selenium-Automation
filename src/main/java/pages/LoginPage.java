package pages;

import org.openqa.selenium.By;
import utils.ConfigReader;
import java.time.Duration;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * LoginPage — page object for the GitHub login flow (https://github.com/login).
 * Author: Jothi Sri
 */
public class LoginPage extends BasePage {
<<<<<<< HEAD

}
=======
    private final By usernameField = By.id("login_field");
    private final By passwordField = By.id("password");
    private final By signInButton = By.cssSelector("input[type='submit'][value='Sign in']");
    private final By errorFlash = By.cssSelector("#js-flash-container .flash-error, " + "#js-flash-container [role='alert'], " + "[role='alert']");
    private final By accountMenuButton = By.cssSelector("button[data-login], img[data-testid='github-avatar']");

    public LoginPage open() {
        navigateTo(ConfigReader.getProperty("base.url") + "/login");
        return this;
    }

    public LoginPage enterUsername(String username) {
        type(usernameField, username);
        return this;
    }

    public LoginPage enterPassword(String password) {
        type(passwordField, password);
        return this;
    }

    public LoginPage clickSignIn() {
        click(signInButton);
        return this;
    }

    /**
     * Performs login with the supplied username and password.
     */
    public LoginPage login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        return clickSignIn();
    }

    /**
     * Logs in using github.username / github.password
     * from config.properties.
     */
    public LoginPage loginFromConfig() {
        String username = ConfigReader.getProperty("github.username");
        String password = ConfigReader.getProperty("github.password");
        return login(username, password);
    }

    public boolean isLoginErrorDisplayed() {
        return isDisplayed(errorFlash);
    }

    public String getErrorMessage() {
        return getText(errorFlash);
    }

    /**
     * True once the post-login account menu is present.
     */
    public boolean isLoggedIn() {
        return isDisplayed(accountMenuButton);
    }

    public String getCurrentPageInfo() {
        return "URL: " + driver.getCurrentUrl() + " | Title: " + driver.getTitle();
    }

    /**
     * Logs in using configured credentials and waits for
     * successful authentication.
     *
     * VERIFICATION PART — KEPT UNCHANGED.
     */
    public LoginPage loginFromConfigAndWaitForLogin() {
        loginFromConfig();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(120));
        wait.until(driver -> isLoggedIn());
        return this;
    }
}
>>>>>>> 8ca7cf66475326cf2e520753cbae49a56e3325e1
