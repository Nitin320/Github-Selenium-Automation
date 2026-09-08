package pages;

import org.openqa.selenium.By;
import utils.ConfigReader;

/**
 * LoginPage — page object for the GitHub login flow (https://github.com/login).
 * Author: Jothi Sri
 */
public class LoginPage extends BasePage {

    private final By usernameField   = By.id("login_field");
    private final By passwordField   = By.id("password");
    private final By signInButton    = By.cssSelector("input[type='submit'][value='Sign in']");
    private final By errorFlash      = By.cssSelector("#js-flash-container .flash-error, .flash-error");
    // Visible only when a session is active — used as the "am I logged in" signal
    // GitHub's header is now a React component (no more <summary> dropdown).
    // data-login only appears on this button when a user is signed in, and
    // data-testid="github-avatar" is GitHub's own stable test hook for the
    // avatar image inside it — either one reliably means "logged in".
    private final By accountMenuButton = By.cssSelector(
            "button[data-login], img[data-testid='github-avatar']");

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

    public LoginPage login(String username, String password) {
        return enterUsername(username).enterPassword(password).clickSignIn();
    }

    /** Logs in using github.username / github.password from config.properties (resolved via env / .env). */
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

    /** True once the post-login header (account menu) is present on the page. */
    public boolean isLoggedIn() {
        return isDisplayed(accountMenuButton);
    }
    public String getCurrentPageInfo() {
        return "URL: " + driver.getCurrentUrl() + " | Title: " + driver.getTitle();
    }
}
