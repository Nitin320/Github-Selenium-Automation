package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ConfigReader;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CodeBrowserPage — page object for browsing the file tree inside a GitHub repository.
 *
 * <p>Responsibilities (Neil Joe):
 * <ul>
 *   <li>Navigate to a repository's root or sub-directory</li>
 *   <li>List folder entries (files and directories)</li>
 *   <li>Click into a folder or file</li>
 *   <li>Read the current breadcrumb path</li>
 * </ul>
 *
 * Author: Neil Joe Augustine
 */
public class CodeBrowserPage extends BasePage {

    // ------------------------------------------------------------------ //
    //  Locators  (GitHub React UI — verified against live DOM)            //
    // ------------------------------------------------------------------ //

    /** Any file or directory name link in the rendered file tree. */
    private final By entryLinks = By.cssSelector(
        "a.Link--primary[href*='/blob/'], a.Link--primary[href*='/tree/']"
    );

    /** Presence sentinel: the filename column rendered by React. */
    private final By fileTreeSentinel = By.cssSelector(
        ".react-directory-filename-column, a.Link--primary[href*='/blob/'], a.Link--primary[href*='/tree/']"
    );

    /** Breadcrumb items. */
    private final By breadcrumbItems = By.cssSelector(
        "nav[aria-label='Breadcrumb'] a, .js-path-segment a"
    );

    // ------------------------------------------------------------------ //
    //  Helpers                                                             //
    // ------------------------------------------------------------------ //

    /** Waits for document.readyState == complete then pauses for React hydration. */
    private void waitForReact() {
        new WebDriverWait(driver, Duration.ofSeconds(20)).until(d ->
            ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete")
        );
        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
    }

    // ------------------------------------------------------------------ //
    //  Navigation                                                          //
    // ------------------------------------------------------------------ //

    public CodeBrowserPage openRepository(String owner, String repo) {
        navigateTo(ConfigReader.getProperty("base.url") + "/" + owner + "/" + repo);
        waitForReact();
        return this;
    }

    public CodeBrowserPage openConfiguredRepository() {
        String owner = ConfigReader.getProperty("github.username");
        String repo  = ConfigReader.getProperty("test.repo", "Hello-World");
        return openRepository(owner, repo);
    }

    public CodeBrowserPage clickEntry(String name) {
        By locator = By.xpath(
            "//a[contains(@class,'Link--primary') and normalize-space()='" + name + "']"
        );
        click(locator);
        waitForReact();
        return this;
    }

    // ------------------------------------------------------------------ //
    //  Queries                                                             //
    // ------------------------------------------------------------------ //

    /**
     * Returns the visible names of all file/folder entries — skips blank-text duplicates.
     */
    public List<String> getEntryNames() {
        return driver.findElements(entryLinks).stream()
                     .map(WebElement::getText)
                     .filter(t -> !t.isBlank())
                     .distinct()
                     .collect(Collectors.toList());
    }

    public boolean isFileTreeVisible() {
        return !driver.findElements(fileTreeSentinel).isEmpty();
    }

    public String getBreadcrumbPath() {
        return driver.findElements(breadcrumbItems).stream()
                     .map(WebElement::getText)
                     .filter(t -> !t.isBlank())
                     .collect(Collectors.joining(" / "));
    }

    public boolean entryExists(String name) {
        return getEntryNames().stream().anyMatch(n -> n.equalsIgnoreCase(name));
    }
}
