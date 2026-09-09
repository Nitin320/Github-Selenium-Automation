package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import utils.ConfigReader;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CommitHistoryPage — page object for viewing a repository's commit history.
 *
 * <p>Responsibilities (Neil Joe):
 * <ul>
 *   <li>Navigate to the commits list for a branch</li>
 *   <li>Read commit messages</li>
 *   <li>Read commit author names</li>
 *   <li>Read commit timestamps</li>
 *   <li>Click into a specific commit to view its details</li>
 *   <li>Read the commit SHA shown on the detail page</li>
 * </ul>
 *
 * Author: Neil Joe Augustine
 */
public class CommitHistoryPage extends BasePage {

    // ------------------------------------------------------------------ //
    //  Locators — commits list                                             //
    // ------------------------------------------------------------------ //

    /** Each commit item in the commits list. */
    private final By commitItems      = By.cssSelector(".TimelineItem, li[class*='commit'], .js-commits-list-item");

    /** Commit message link inside each item. */
    private final By commitMessages   = By.cssSelector("a.Link--primary[href*='/commit/'], .markdown-title a[href*='/commit/']");

    /** Author name span inside each commit item. */
    private final By commitAuthors    = By.cssSelector("a[rel='author'], span[data-hovercard-type='user']");

    /** Relative timestamp element ("<time>" or "ago" span). */
    private final By commitTimestamps = By.cssSelector("relative-time, time-ago, time[datetime]");

    // ------------------------------------------------------------------ //
    //  Locators — commit detail page                                       //
    // ------------------------------------------------------------------ //

    /** The full or short SHA shown on a commit detail page. */
    private final By commitSha        = By.cssSelector(".js-clipboard-copy[data-clipboard-text], code.commit-sha, span[class*='sha']");

    /** The commit message heading on the detail page. */
    private final By commitMessageHeading = By.cssSelector(".commit-title, h1.commit-title");

    // ------------------------------------------------------------------ //
    //  Navigation                                                          //
    // ------------------------------------------------------------------ //

    /**
     * Opens the commit history for {@code owner/repo} on the specified branch.
     *
     * @param owner  GitHub username or organisation
     * @param repo   repository name
     * @param branch branch name (e.g. "main")
     */
    public CommitHistoryPage openCommits(String owner, String repo, String branch) {
        navigateTo(ConfigReader.getProperty("base.url")
                   + "/" + owner + "/" + repo + "/commits/" + branch);
        return this;
    }

    /**
     * Opens commit history for the configured {@code test.repo} on the given branch.
     */
    public CommitHistoryPage openConfiguredCommits(String branch) {
        String owner = ConfigReader.getProperty("github.username");
        String repo  = ConfigReader.getProperty("test.repo", "Hello-World");
        return openCommits(owner, repo, branch);
    }

    /**
     * Clicks the commit message link for the commit at position {@code index}
     * (0-based) in the currently visible list.
     */
    public CommitHistoryPage clickCommit(int index) {
        List<WebElement> links = driver.findElements(commitMessages);
        if (index >= links.size()) {
            throw new IndexOutOfBoundsException(
                "Commit index " + index + " out of range; only " + links.size() + " commits visible");
        }
        links.get(index).click();
        return this;
    }

    // ------------------------------------------------------------------ //
    //  Queries — list page                                                 //
    // ------------------------------------------------------------------ //

    /**
     * Returns the visible commit messages on the current page.
     */
    public List<String> getCommitMessages() {
        return driver.findElements(commitMessages).stream()
                     .map(WebElement::getText)
                     .filter(t -> !t.isBlank())
                     .collect(Collectors.toList());
    }

    /**
     * Returns the visible commit author names on the current page.
     */
    public List<String> getCommitAuthors() {
        return driver.findElements(commitAuthors).stream()
                     .map(WebElement::getText)
                     .filter(t -> !t.isBlank())
                     .collect(Collectors.toList());
    }

    /**
     * Returns the {@code datetime} attribute values of all visible timestamps.
     */
    public List<String> getCommitTimestamps() {
        return driver.findElements(commitTimestamps).stream()
                     .map(el -> el.getAttribute("datetime"))
                     .filter(t -> t != null && !t.isBlank())
                     .collect(Collectors.toList());
    }

    /**
     * Returns {@code true} when at least one commit entry is visible in the list.
     */
    public boolean isCommitListVisible() {
        return !driver.findElements(commitMessages).isEmpty();
    }

    /**
     * Returns the total number of commits visible on the current page.
     */
    public int getCommitCount() {
        return driver.findElements(commitMessages).size();
    }

    // ------------------------------------------------------------------ //
    //  Queries — detail page                                               //
    // ------------------------------------------------------------------ //

    /**
     * Returns the short or full SHA string shown on a commit detail page.
     */
    public String getCommitSha() {
        WebElement shaEl = driver.findElements(commitSha).stream().findFirst()
            .orElseThrow(() -> new org.openqa.selenium.NoSuchElementException("Commit SHA element not found"));
        String sha = shaEl.getAttribute("data-clipboard-text");
        return (sha != null && !sha.isBlank()) ? sha : shaEl.getText();
    }

    /**
     * Returns the commit message heading text on a commit detail page.
     */
    public String getCommitMessageHeading() {
        return getText(commitMessageHeading);
    }
}
