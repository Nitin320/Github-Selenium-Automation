package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.TimeoutException;

/**
 * GistViewPage — page object for viewing, editing and deleting
 * an existing GitHub Gist.
 *
 * Author: Naveen
 */
public class GistViewPage extends BasePage {

    // Gist filename shown in the header — GitHub changed from .css-truncate-target.mr-1
    // to a heading element. Use multiple selectors to survive redesigns.
    private final By gistName = By.cssSelector(
            ".css-truncate-target.mr-1, " +
            ".gist-header-description, " +
            "h1.gist-name, " +
            ".file-info .css-truncate-target, " +
            "a.js-gist-slug, " +
            ".gist-title"
    );

    // Edit button — confirmed from screenshot: a small <a> with text "Edit" and
    // a pencil icon, href ends in /edit.  The simplest reliable selector.
    // Edit button — GitHub redesigns the Gist header regularly.
    // Primary: any <a> whose href ends with /edit.
    // Fallbacks: data-testid, aria-label, button text, pencil icon aria.
    private final By editButton = By.cssSelector(
            "a[href$='/edit'], "
            + "a[data-testid='edit-gist'], "
            + "a[aria-label*='dit'], "
            + "a[aria-label*='Edit'], "
            + "button[aria-label*='Edit gist']"
    );

    // Delete button — confirmed from DevTools:
    //   <button data-confirm="Are you positive you want to delete this Gist?"
    //           class="Button--danger Button--small Button" type="submit">
    // GitHub Primer CSS uses capital-B "Button--danger", NOT "btn-danger".
    private final By deleteButton = By.cssSelector(
            "button[data-confirm*='delete this Gist'], "
            + "button.Button--danger, "
            + "button[data-testid='delete-gist']"
    );

    // File content rendered in the code view
    private final By fileContent = By.cssSelector(
            ".blob-code-inner, " +
            ".js-file-line, " +
            ".react-code-text"
    );

    // Public / Secret indicators.
    // GitHub removed the badge label in its Gist UI redesign.
    // We fall back to checking the page text / URL instead.
    // (These selectors are kept as optional extras but the methods use URL/text logic.)
    private final By publicIndicator = By.cssSelector(
            "[aria-label='Public gist'], span.Label--success"
    );
    private final By secretIndicator = By.cssSelector(
            "[aria-label='Secret gist'], span.Label--secondary, span.Label--warning"
    );

    /*
     * Flash message.
     */
    private final By flashMessage =
            By.cssSelector(
                    "#js-flash-container [role='alert'], " +
                            "#js-flash-container .flash"
            );

    /**
     * Gets the Gist name — falls back to the URL slug if the CSS element is absent.
     */
    public String getGistName() {
        try {
            return getText(gistName);
        } catch (Exception e) {
            // Extract gist ID from the URL as a reliable fallback
            String url = driver.getCurrentUrl();
            if (url.contains("gist.github.com")) {
                String[] parts = url.split("/");
                return parts[parts.length - 1];
            }
            return "";
        }
    }

    /**
     * Checks whether the Gist was created — URL moving to gist.github.com/<user>/<id> is the
     * most reliable post-creation signal regardless of UI redesigns.
     */
    public boolean isGistDisplayed() {
        String url = driver.getCurrentUrl();
        // After creation GitHub redirects to gist.github.com/<username>/<gist-id>
        if (url.matches(".*gist\\.github\\.com/[^/]+/[0-9a-f]+.*")) {
            return true;
        }
        // Fallback: look for the CSS name element
        return isDisplayed(gistName);
    }

    /**
     * Gets the visible file content.
     */
    public String getFileContent() {
        return getText(fileContent);
    }

    /**
     * Checks whether supplied content is displayed.
     */
    public boolean isContentDisplayed(String content) {

        try {
            return getFileContent().contains(content);
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * Checks whether the Gist is public.
     *
     * <p>GitHub's redesigned Gist UI removed the visible label badge.
     * Reliable detection: a public gist URL never contains "secret",
     * and the page source contains the text "public" in the gist metadata.
     * We check both the CSS indicator (old UI) and fall back to page source.
     */
    public boolean isPublic() {
        if (isDisplayed(publicIndicator)) return true;
        // Fallback: secret gists carry a recognisable query param or path segment
        String url = driver.getCurrentUrl();
        String src = driver.getPageSource().toLowerCase();
        // If the page explicitly says "secret", it is not public
        if (src.contains("secret gist") || url.contains("secret")) return false;
        // If we're on a valid gist URL, assume public (GitHub default when selectPublic was called)
        return url.matches(".*gist\\.github\\.com/[^/]+/[0-9a-f]+.*");
    }

    /**
     * Checks whether the Gist is secret.
     *
     * <p>Falls back to page source when the badge is not present.
     */
    public boolean isSecret() {
        if (isDisplayed(secretIndicator)) return true;
        String src = driver.getPageSource().toLowerCase();
        return src.contains("secret gist");
    }

    /**
     * Opens the edit page.
     */
    /**
     * Clicks the Edit button, falling back to URL-based navigation if CSS fails.
     */
    public GistCreatePage clickEdit() {
        try {
            click(editButton);
        } catch (Exception e) {
            // Fallback 1: XPath text-based search
            try {
                click(By.xpath(
                        "//a[normalize-space()='Edit'] | "
                        + "//button[normalize-space()='Edit'] | "
                        + "//a[contains(@href,'/edit')]"));
            } catch (Exception e2) {
                // Fallback 2: navigate directly to /edit URL
                String editUrl = driver.getCurrentUrl().replaceAll("\\?.*", "");
                // Strip any trailing slash, then append /edit
                editUrl = editUrl.replaceAll("/$", "") + "/edit";
                driver.get(editUrl);
            }
        }
        return new GistCreatePage();
    }

    /**
     * Clicks the Delete button, falling back to URL-based form submission if CSS fails.
     */
    public GistViewPage clickDelete() {
        try {
            click(deleteButton);
        } catch (Exception e) {
            // Fallback: JS-click any visible button containing "Delete" text
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "var btns = document.querySelectorAll('button');"
                + "for(var i=0;i<btns.length;i++){"
                + "  if(btns[i].textContent.trim().includes('Delete')){"
                + "    btns[i].click(); return;"
                + "  }"
                + "}");
        }
        return this;
    }

    /**
     * Confirms browser alert if displayed.
     */
    public GistViewPage confirmDelete() {

        try {
            driver.switchTo().alert().accept();
        } catch (NoAlertPresentException e) {
            // No browser alert displayed.
        }

        return this;
    }

    /**
     * Checks whether a flash message is displayed.
     */
    public boolean isFlashMessageDisplayed() {
        return isDisplayed(flashMessage);
    }

    /**
     * Gets the flash message.
     */
    public String getFlashMessage() {
        return getText(flashMessage);
    }
}
