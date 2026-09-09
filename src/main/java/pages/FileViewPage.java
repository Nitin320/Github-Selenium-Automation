package pages;

import org.openqa.selenium.By;
import utils.ConfigReader;

/**
 * FileViewPage — page object for viewing a single file on GitHub.
 *
 * <p>Responsibilities (Neil Joe):
 * <ul>
 *   <li>Navigate directly to a file URL</li>
 *   <li>Read the displayed file content (rendered blob view)</li>
 *   <li>Switch to the raw view and read the raw source</li>
 *   <li>Read the file name shown in the breadcrumb</li>
 *   <li>Detect language badge / syntax highlighting</li>
 * </ul>
 *
 * Author: Neil Joe Augustine
 */
public class FileViewPage extends BasePage {

    // ------------------------------------------------------------------ //
    //  Locators                                                            //
    // ------------------------------------------------------------------ //

    /** Each line of the rendered code blob. */
    private final By codeLines       = By.cssSelector("table.highlight td.blob-code, .react-code-lines .react-code-line-contents");

    /** The "Raw" button that opens the raw file URL. */
    private final By rawButton       = By.cssSelector("a#raw-url, a[data-testid='raw-button'], a[href*='/raw/']");

    /** The file-name segment at the end of the breadcrumb. */
    private final By fileNameBreadcrumb = By.cssSelector("nav[aria-label='Breadcrumb'] .final-path, .js-path-segment:last-child a, span[class*='final-path']");

    /** Language badge shown above the blob (e.g. "Java", "Python"). */
    private final By languageBadge   = By.cssSelector("[class*='language'] span, .blob-num ~ td .pl-ent");

    /** Raw page: the pre element containing the full raw text. */
    private final By rawContent      = By.cssSelector("pre");

    // ------------------------------------------------------------------ //
    //  Navigation                                                          //
    // ------------------------------------------------------------------ //

    /**
     * Opens the blob view for a specific file path inside {@code owner/repo}.
     *
     * @param owner    GitHub username or organisation
     * @param repo     repository name
     * @param branch   branch name (e.g. "main")
     * @param filePath path relative to repo root (e.g. "README.md")
     */
    public FileViewPage openFile(String owner, String repo, String branch, String filePath) {
        navigateTo(ConfigReader.getProperty("base.url")
                   + "/" + owner + "/" + repo + "/blob/" + branch + "/" + filePath);
        return this;
    }

    /**
     * Clicks the "Raw" button to switch to the plain-text raw view.
     * After this call the current URL changes to the raw content URL.
     */
    public FileViewPage viewRaw() {
        click(rawButton);
        return this;
    }

    // ------------------------------------------------------------------ //
    //  Queries                                                             //
    // ------------------------------------------------------------------ //

    /**
     * Returns the full text content of all rendered code lines joined by newlines.
     */
    public String getFileContent() {
        StringBuilder sb = new StringBuilder();
        driver.findElements(codeLines).forEach(el -> sb.append(el.getText()).append("\n"));
        return sb.toString().trim();
    }

    /**
     * Returns the raw source text from the raw-view {@code <pre>} element.
     * Call {@link #viewRaw()} first.
     */
    public String getRawContent() {
        return getText(rawContent);
    }

    /**
     * Returns the file name shown at the end of the page breadcrumb.
     */
    public String getDisplayedFileName() {
        return getText(fileNameBreadcrumb);
    }

    /**
     * Returns {@code true} when the code blob area is present and visible.
     */
    public boolean isFileContentVisible() {
        return isDisplayed(codeLines);
    }

    /**
     * Returns {@code true} when the Raw button is present and clickable.
     */
    public boolean isRawButtonVisible() {
        return isDisplayed(rawButton);
    }

    /**
     * Returns the current page URL, useful for asserting the raw URL pattern.
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
