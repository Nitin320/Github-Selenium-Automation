package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * NewRepoPage — page object for GitHub's new repository creation form.
 * Author: Sujin
 */
public class NewRepoPage extends BasePage {

    // The /new form has no id or aria-label on the repo-name input (confirmed from
    // screenshots). It is the FIRST single-line text input in the form — the
    // description field comes after it, and all the other inputs are dropdowns.
    // We locate it via a broad CSS selector and fall back to JS injection if none
    // of the explicit selectors match.
    private final By repoNameInput = By.cssSelector(
            "input#repository_name, "
            + "input[name='repository[name]'], "
            + "input[aria-label='Repository name'], "
            + "input[aria-label*='epository name'], "
            + "input[placeholder*='epository'], "
            + "input[autocomplete='repository-name'], "
            + "input[data-testid='repo-name-input']"
    );

    // "Create repository" button — confirmed from screenshot text label.
    private final By createRepoButton = By.xpath(
            "//button[contains(normalize-space(),'Create repository')] | "
            + "//button[@data-target='new-repository.submitButton'] | "
            + "//button[@data-testid='create-repo-button'] | "
            + "//button[@type='submit' and not(@aria-label)]"
    );

    /**
     * Enters the repository name.
     *
     * <p>GitHub's /new React form renders the repo-name as a plain {@code <input>}
     * with no stable id or aria-label (confirmed from screenshots — the field has
     * the label "Repository name *" but the input element itself carries no
     * identifying attributes).
     *
     * <p>Strategy: try the explicit selector list first; if nothing is found after
     * 5 s, fall back to a JavaScript heuristic that picks the first short
     * single-line text input inside the form that is not the description area.
     */
    public NewRepoPage enterRepoName(String name) {
        org.openqa.selenium.JavascriptExecutor js =
                (org.openqa.selenium.JavascriptExecutor) driver;

        // Wait up to 30 s for the page to contain ANY text input (form is mounted)
        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(30))
                .until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("form input[type='text'], input:not([type])")));

        // Try the explicit selector chain first
        org.openqa.selenium.WebElement input = null;
        try {
            input = new org.openqa.selenium.support.ui.WebDriverWait(
                    driver, java.time.Duration.ofSeconds(5))
                    .until(ExpectedConditions.presenceOfElementLocated(repoNameInput));
        } catch (Exception ignored) { /* fall through to JS heuristic */ }

        // JS heuristic: first single-line text input in the form that is blank and short
        if (input == null) {
            input = (org.openqa.selenium.WebElement) js.executeScript(
                    "var inputs = document.querySelectorAll('form input[type=\"text\"], form input:not([type])');"
                    + "for (var i = 0; i < inputs.length; i++) {"
                    + "  var el = inputs[i];"
                    + "  if (el.offsetParent !== null && !el.readOnly && !el.disabled) return el;"
                    + "}"
                    + "return null;");
        }

        if (input == null) {
            throw new org.openqa.selenium.NoSuchElementException(
                    "Could not locate the repository name input on /new");
        }

        // Scroll into view, focus, clear, type
        js.executeScript("arguments[0].scrollIntoView(true); arguments[0].focus();", input);
        input.clear();
        input.sendKeys(name);
        return this;
    }

    /**
     * Clicks the "Create repository" button and waits for GitHub to redirect
     * away from {@code /new} to the newly created repository page.
     *
     * <p>GitHub's React form submits asynchronously — the redirect can take
     * 3–8 seconds.  Without this wait the caller reads the title while the
     * URL is still {@code /new}, which always returns "new" as the repo name.
     */
    public NewRepoPage clickCreateRepository() {
        click(createRepoButton);
        // Wait until the URL no longer contains "/new" (redirect completed)
        new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(30))
                .until(d -> !d.getCurrentUrl().contains("/new"));
        return this;
    }
}
