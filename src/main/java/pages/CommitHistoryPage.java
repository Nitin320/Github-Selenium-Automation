package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.ConfigReader;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CommitHistoryPage — page object for viewing a repository's commit history.
 *
 * <p>Responsibilities (Neil Joe):
 * <ul>
 *   <li>Navigate to the commits list for a branch</li>
 *   <li>Read commit messages</li>
 *   <li>Click into a specific commit to view its details</li>
 *   <li>Read the commit SHA shown on the detail page</li>
 * </ul>
 *
 * Author: Neil Joe Augustine
 */
public class CommitHistoryPage extends BasePage {

    // ------------------------------------------------------------------ //
    //  Locators  (GitHub React UI — verified against live DOM)            //
    // ------------------------------------------------------------------ //

    /**
     * Commit title links on the history list page.
     * GitHub renders these as <a class="color-fg-default" href="…/commit/SHA">.
     * There can be 2 per commit (title + PR sub-line) — we de-duplicate by href.
     */
    private final By commitMessageLinks = By.cssSelector(
        "a.color-fg-default[href*='/commit/']"
    );

    /** Short SHA button/link on the list page. */
    private final By shortShaLinks = By.cssSelector(
        "a.prc-Button-ButtonBase-9n-Xk[href*='/commit/'], a[class*='prc-Button'][href*='/commit/']"
    );

    /**
     * Commit message heading on the detail page.
     * Live DOM: <h1>Commit 7fd1a60</h1> — the first h1 on the page.
     */
    private final By commitMessageHeading = By.cssSelector("h1");

    /**
     * Short SHA on the commit detail page.
     * Live DOM: <span class="text-mono">7fd1a60</span>
     */
    private final By commitShaMono = By.cssSelector("span.text-mono");

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

    public CommitHistoryPage openCommits(String owner, String repo, String branch) {
        navigateTo(ConfigReader.getProperty("base.url")
                   + "/" + owner + "/" + repo + "/commits/" + branch);
        waitForReact();
        return this;
    }

    public CommitHistoryPage openConfiguredCommits(String branch) {
        String owner = ConfigReader.getProperty("github.username");
        String repo  = ConfigReader.getProperty("test.repo", "Hello-World");
        return openCommits(owner, repo, branch);
    }

    /**
     * Clicks the first commit message link at position {@code index} (0-based)
     * in the de-duplicated commit list.
     */
    public CommitHistoryPage clickCommit(int index) {
        List<WebElement> deduped = getDeduplicatedCommitLinks();
        if (index >= deduped.size()) {
            throw new IndexOutOfBoundsException(
                "Commit index " + index + " out of range; only " + deduped.size() + " commits visible");
        }
        deduped.get(index).click();
        waitForReact();
        return this;
    }

    // ------------------------------------------------------------------ //
    //  Queries — list page                                                 //
    // ------------------------------------------------------------------ //

    /**
     * Returns one link element per unique commit SHA (de-duplicates title + PR sub-line).
     */
    private List<WebElement> getDeduplicatedCommitLinks() {
        List<WebElement> all = driver.findElements(commitMessageLinks);
        Set<String> seen = new LinkedHashSet<>();
        List<WebElement> deduped = new ArrayList<>();
        for (WebElement el : all) {
            String href = el.getAttribute("href");
            // Normalise: strip #comments fragment if present
            if (href != null) href = href.replaceAll("#.*$", "");
            if (href != null && seen.add(href)) {
                deduped.add(el);
            }
        }
        return deduped;
    }

    public List<String> getCommitMessages() {
        return getDeduplicatedCommitLinks().stream()
                     .map(WebElement::getText)
                     .filter(t -> !t.isBlank())
                     .collect(Collectors.toList());
    }

    public boolean isCommitListVisible() {
        return !driver.findElements(commitMessageLinks).isEmpty();
    }

    public int getCommitCount() {
        return getDeduplicatedCommitLinks().size();
    }

    // ------------------------------------------------------------------ //
    //  Queries — detail page                                               //
    // ------------------------------------------------------------------ //

    public String getCommitSha() {
        // Primary: span.text-mono (confirmed on live DOM, e.g. "7fd1a60")
        List<WebElement> monoEls = driver.findElements(commitShaMono);
        for (WebElement el : monoEls) {
            String text = el.getText().trim();
            if (text.matches("[0-9a-f]{7,40}")) return text;
        }
        // Fallback: extract from current URL
        String url = driver.getCurrentUrl();
        if (url.contains("/commit/")) {
            return url.replaceAll(".*/commit/([0-9a-f]+).*", "$1");
        }
        throw new org.openqa.selenium.NoSuchElementException("Could not find commit SHA on page: " + url);
    }

    public String getCommitMessageHeading() {
        // Live DOM: <h1>Commit 7fd1a60</h1>
        return getText(commitMessageHeading);
    }
}
