package pages;

import driver.DriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.AdaptiveWait;
import utils.ScreenshotUtils;

import java.time.Duration;

/**
 * BasePage — common page utilities shared by all page objects.
 *
 * <p>All wait operations delegate to {@link AdaptiveWait}, which self-calibrates
 * its timeout windows based on real observed element response times during the
 * current test run.  Fixed implicit waits are intentionally absent — mixing
 * implicit and explicit waits produces unreliable timeout behaviour.
 *
 * Author: Nitin  |  Adaptive timing: Group 5
 */
public class BasePage {

    protected WebDriver driver;

    /**
     * Legacy {@code wait} field kept for backwards compatibility with page objects
     * that construct their own {@link WebDriverWait} inline (e.g. GistCreatePage,
     * ProfilePage).  New code should call the {@code waitFor*()} helpers which
     * route through {@link AdaptiveWait}.
     */
    protected WebDriverWait wait;

    public BasePage() {
        this.driver = DriverManager.getDriver();
        // Provide a fallback WebDriverWait for any page objects that reference
        // this.wait directly. AdaptiveWait still handles all calls through the
        // helpers below — this is only a safety net for legacy inline usages.
        long defaultSec = Long.parseLong(
                utils.ConfigReader.getProperty("explicit.wait", "15"));
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(defaultSec));
        PageFactory.initElements(driver, this);
    }

    // ── Wait helpers — all route through AdaptiveWait ────────────────────────

    /**
     * Waits for the element to be visible using an adaptive timeout derived from
     * historical response times for this locator.
     */
    protected WebElement waitForVisibility(By locator) {
        return AdaptiveWait.waitForVisibility(driver, locator);
    }

    /**
     * Waits for the element to be clickable using an adaptive timeout.
     */
    protected WebElement waitForClickable(By locator) {
        return AdaptiveWait.waitForClickable(driver, locator);
    }

    protected void click(By locator) {
        waitForClickable(locator).click();
    }

    protected void type(By locator, String text) {
        WebElement el = waitForClickable(locator);
        try {
            el.clear();
        } catch (InvalidElementStateException e) {
            // GitHub's live page occasionally reports an element as clickable
            // a beat before it will accept clear()/sendKeys() (a repaint,
            // animation, or focus shift in progress). One short re-wait and
            // retry is enough to ride that out; a real interactability
            // problem will fail again and surface normally.
            el = AdaptiveWait.waitForClickable(driver, locator);
            el.clear();
        }
        el.sendKeys(text);
    }

    protected String getText(By locator) {
        return waitForVisibility(locator).getText();
    }

    protected boolean isDisplayed(By locator) {
        try {
            return AdaptiveWait.isPresent(driver, locator);
        } catch (TimeoutException | NoSuchElementException e) {
            return false;
        }
    }

    protected void navigateTo(String url) {
        driver.get(url);
    }

    protected void takeScreenshot(String name) {
        ScreenshotUtils.capture(name);
    }
}
