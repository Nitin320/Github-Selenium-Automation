package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ConfigReader;

import java.time.Duration;

/**
 * FileViewPage — page object for viewing a single file on GitHub.
 *
 * <p>Responsibilities (Neil Joe):
 * <ul>
 *   <li>Navigate directly to a file URL</li>
 *   <li>Read the displayed file content (rendered blob view)</li>
 *   <li>Switch to the raw view and read the raw source</li>
 *   <li>Detect Raw button presence</li>
 * </ul>
 *
 * Author: Neil Joe Augustine
 */
public class FileViewPage extends BasePage {

    // ------------------------------------------------------------------ //
    //  Locators  (GitHub React UI — verified against live DOM)            //
    // ------------------------------------------------------------------ //

    /** Rendered code lines container (React code view). */
    private final By codeLines = By.cssSelector(".react-code-lines");

    /** The "Raw" button (data-testid confirmed on live DOM). */
    private final By rawButton = By.cssSelector("a[data-testid='raw-button']");

    /** Raw page: the pre element containing the full raw text. */
    private final By rawContent = By.cssSelector("pre");

    // ------------------------------------------------------------------ //
    //  Helpers                                                             //
    // ------------------------------------------------------------------ //

    private void waitForReact() {
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(d ->
            ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete")
        );
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
    }

    // ------------------------------------------------------------------ //
    //  Navigation                                                          //
    // ------------------------------------------------------------------ //

    public FileViewPage openFile(String owner, String repo, String branch, String filePath) {
        navigateTo(ConfigReader.getProperty("base.url")
                   + "/" + owner + "/" + repo + "/blob/" + branch + "/" + filePath);
        waitForReact();
        return this;
    }

    /**
     * Clicks the "Raw" button and waits for navigation to the raw content host.
     * GitHub redirects to raw.githubusercontent.com after clicking Raw.
     */
    public FileViewPage viewRaw() {
        click(rawButton);
        // Wait for navigation away from github.com to the raw host
        new WebDriverWait(driver, Duration.ofSeconds(15)).until(d ->
            d.getCurrentUrl().contains("raw.githubusercontent.com") || d.getCurrentUrl().contains("/raw/")
        );
        return this;
    }

    // ------------------------------------------------------------------ //
    //  Queries                                                             //
    // ------------------------------------------------------------------ //

    public String getFileContent() {
        return getText(codeLines);
    }

    public String getRawContent() {
        return getText(rawContent);
    }

    public boolean isFileContentVisible() {
        return isDisplayed(codeLines);
    }

    public boolean isRawButtonVisible() {
        return isDisplayed(rawButton);
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
