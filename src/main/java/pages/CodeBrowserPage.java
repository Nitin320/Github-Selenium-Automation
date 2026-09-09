package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import utils.ConfigReader;

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
    //  Locators                                                            //
    // ------------------------------------------------------------------ //

    /** Rows in the repository file table (both files and directories). */
    private final By fileTableRows    = By.cssSelector("table.files tbody tr, [aria-label='Files'] .Box-row");

    /** File/folder name link inside each row. */
    private final By entryNameLink    = By.cssSelector("a.js-navigation-open[role='rowheader'], a[data-pjax='#repo-content-pjax-container']");

    /** Breadcrumb items that make up the current path. */
    private final By breadcrumbItems  = By.cssSelector("nav[aria-label='Breadcrumb'] a, .js-path-segment a");

    /** The repository name heading on the code tab. */
    private final By repoNameHeading  = By.cssSelector("strong[itemprop='name'] a, h1[itemprop='name'] a");

    /** "Code" tab that returns to the file tree from any sub-page. */
    private final By codeTab          = By.cssSelector("a[data-tab-item='code'], a#code-tab");

    // ------------------------------------------------------------------ //
    //  Navigation                                                          //
    // ------------------------------------------------------------------ //

    /**
     * Opens the root file tree for {@code owner/repo}.
     *
     * @param owner GitHub username or organisation
     * @param repo  repository name
     * @return this page for fluent chaining
     */
    public CodeBrowserPage openRepository(String owner, String repo) {
        navigateTo(ConfigReader.getProperty("base.url") + "/" + owner + "/" + repo);
        return this;
    }

    /**
     * Opens the root file tree for the configured {@code test.repo} repository.
     * The owner is derived from {@code github.username}.
     */
    public CodeBrowserPage openConfiguredRepository() {
        String owner = ConfigReader.getProperty("github.username");
        String repo  = ConfigReader.getProperty("test.repo", "Hello-World");
        return openRepository(owner, repo);
    }

    /**
     * Navigates into a folder or file by its visible name in the file tree.
     *
     * @param name exact file or folder name shown in the table
     */
    public CodeBrowserPage clickEntry(String name) {
        By locator = By.xpath(
            "//a[contains(@class,'js-navigation-open') and normalize-space()='" + name + "']" +
            " | //a[@data-pjax='#repo-content-pjax-container' and normalize-space()='" + name + "']"
        );
        click(locator);
        return this;
    }

    // ------------------------------------------------------------------ //
    //  Queries                                                             //
    // ------------------------------------------------------------------ //

    /**
     * Returns the visible names of all entries in the current directory.
     */
    public List<String> getEntryNames() {
        List<WebElement> links = driver.findElements(entryNameLink);
        return links.stream()
                    .map(WebElement::getText)
                    .filter(t -> !t.isBlank())
                    .collect(Collectors.toList());
    }

    /**
     * Returns {@code true} when the file tree table is present on the page.
     */
    public boolean isFileTreeVisible() {
        return isDisplayed(fileTableRows);
    }

    /**
     * Returns the current breadcrumb path segments joined by " / ".
     */
    public String getBreadcrumbPath() {
        return driver.findElements(breadcrumbItems).stream()
                     .map(WebElement::getText)
                     .filter(t -> !t.isBlank())
                     .collect(Collectors.joining(" / "));
    }

    /**
     * Returns {@code true} when an entry with the given name exists in the tree.
     */
    public boolean entryExists(String name) {
        return getEntryNames().stream().anyMatch(n -> n.equalsIgnoreCase(name));
    }

    /** Clicks the "Code" tab to return to the repository root file tree. */
    public CodeBrowserPage goToCodeTab() {
        click(codeTab);
        return this;
    }
}
