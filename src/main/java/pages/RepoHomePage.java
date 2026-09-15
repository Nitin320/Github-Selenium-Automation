package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * RepoHomePage — page object for a GitHub repository's home page.
 * Author: Sujin
 */
public class RepoHomePage extends BasePage {

    // After creation the repo home page shows the repo name in the breadcrumb header.
    // GitHub's layout: <strong itemprop="name"><a>repo-name</a></strong> (classic)
    // or a heading-level element in the React layout. Try multiple.
    private final By repoTitleHeader = By.cssSelector(
            "strong[itemprop='name'] a, "
            + "h1[itemprop='name'] a, "
            + "[data-testid='repo-title-name'], "
            + ".AppHeader-context-full a[href*='/'], "
            + "nav[aria-label='Repository'] a, "
            + "[data-pjax='#repo-content-pjax-container'] h1 a"
    );
    private final By settingsTab = By.cssSelector(
            "#settings-tab, "
            + "a[data-tab-item='settings-tab'], "
            + "a[href*='/settings']"
    );

    // ── Settings / Danger Zone locators ──────────────────────────────────────
    // GitHub's "Delete this repository" button is in the Danger Zone section of Settings.
    private final By deleteRepoButton    = By.xpath(
            "//summary[contains(normalize-space(.),'Delete this repository')]");
    private final By deleteConfirmInput  = By.cssSelector("input#verification_field");
    private final By deleteConfirmButton = By.cssSelector(
            "button[data-disable-with]");

    /**
     * Returns the repository name shown in the page header.
     * Falls back to extracting it from the URL if the CSS element is not found.
     */
    public String getRepoTitleText() {
        // First ensure the page has actually loaded the repo (not still on /new or blank)
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(15))
                    .until(d -> !d.getCurrentUrl().endsWith("/new")
                             && !d.getCurrentUrl().contains("/new?"));
        } catch (Exception ignored) { /* best effort — proceed to read */ }

        try {
            return getText(repoTitleHeader);
        } catch (Exception e) {
            // Fallback: extract repo name from the URL  github.com/owner/repo-name
            String url = driver.getCurrentUrl();
            String[] parts = url.replaceAll("\\?.*", "").replaceAll("/$", "").split("/");
            return parts.length >= 2 ? parts[parts.length - 1] : "";
        }
    }

    public void clickSettings() {
        click(settingsTab);
    }

    /**
     * Navigates the Danger Zone deletion flow: clicks "Delete this repository",
     * confirms by typing the full repository name, then submits.
     *
     * <p>The caller must already be on the repository settings page.
     *
     * @param repoName the full "{owner}/{repo}" or just "{repo}" token —
     *                 GitHub will accept whatever text the confirmation dialog requests.
     */
    public void deleteRepository(String repoName) {
        // Scroll to the Danger Zone and open the delete dialog
        click(deleteRepoButton);

        // GitHub shows a dialog; wait for the confirmation input
        wait.until(ExpectedConditions.visibilityOfElementLocated(deleteConfirmInput));

        // Type the confirmation token that GitHub asks for
        type(deleteConfirmInput, repoName);

        // Submit — the button text changes to "I understand the consequences…"
        click(deleteConfirmButton);
    }
}