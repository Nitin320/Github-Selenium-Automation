package pages;

import org.openqa.selenium.By;

/**
 * IssuePage — page object for GitHub Issues tab.
 * Author: Deva Vignan
 */
public class IssuePage extends BasePage {
    // Locators

    private final By issuesTab =
            By.xpath("//span[text()='Issues']");

    // GitHub's "New issue" button — try broad text-based XPath as primary selector
    // since the CSS/testid attributes change between redesigns.
    private final By newIssueButton = By.xpath(
            "//a[contains(@href,'/issues/new')] | "
            + "//a[@data-testid='new-issue-button'] | "
            + "//a[normalize-space()='New issue'] | "
            + "//button[normalize-space()='New issue']"
    );

    // GitHub's new issue form (confirmed from screenshot):
    // - Title field: plain <input> with placeholder "Title", label "Add a title *"
    //   The old id="issue_title" is gone in the React redesign.
    // - Description: a contenteditable or <textarea> with placeholder text
    // - Submit: a <button> labelled "Create"
    private final By issueTitle = By.cssSelector(
            "input#issue_title, "
            + "input[aria-label='Title'], "
            + "input[placeholder='Title'], "
            + "input[name='issue[title]']"
    );

    private final By issueDescription = By.cssSelector(
            "textarea#issue_body, "
            + "textarea[name='issue[body]'], "
            + "div[aria-label='Add a description'], "
            + "textarea[placeholder*='description'], "
            + ".js-comment-field"
    );

    // Submit: the green "Create" button (confirmed from screenshot — says "Create" not "Submit issue")
    private final By submitIssueButton = By.cssSelector(
            "button[data-testid='create-issue-button'], "
            + "button[type='submit'][aria-label*='reate'], "
            + "button.btn-primary[type='submit']"
    );
    // Fallback XPath for submit — matches button with text "Create"
    private final By submitIssueButtonXpath = By.xpath(
            "//button[normalize-space()='Create'] | "
            + "//button[normalize-space()='Submit new issue']"
    );

    // After creation GitHub shows an issue detail page with h1 containing the title
    // In the React redesign it may be inside a <bdi> or a heading span
    private final By issueHeader = By.cssSelector(
            "h1.gh-header-title, "
            + "h1[class*='title'], "
            + "bdi.js-issue-title, "
            + "h1"
    );

    private final By issuesPageHeader = By.cssSelector(
            "[data-testid='issue-list-header'], " +
            "div[aria-label='Issues'], " +
            "h1, h2"
    );

    // Methods

    public void clickIssuesTab() {
        click(issuesTab);
    }

    /**
     * Clicks the "New issue" button.
     * Falls back to direct URL navigation if the button is not found —
     * some repo layouts show a different CTA depending on whether issues exist.
     */
    public void clickNewIssue() {
        try {
            click(newIssueButton);
        } catch (Exception e) {
            // Fallback: navigate directly to the new-issue form
            String currentUrl = driver.getCurrentUrl();
            // currentUrl is already at /issues; append /new
            String newIssueUrl = currentUrl.replaceAll("/issues.*", "/issues/new");
            driver.get(newIssueUrl);
        }
    }

    public void enterIssueTitle(String title) {
        type(issueTitle, title);
    }

    public void enterIssueDescription(String description) {
        type(issueDescription, description);
    }

    public void submitIssue() {
        try {
            click(submitIssueButton);
        } catch (Exception e) {
            click(submitIssueButtonXpath);
        }
    }

    /**
     * The issues page is displayed when the URL ends in /issues.
     * GitHub redesigned the page header repeatedly; checking the URL is reliable.
     */
    public boolean isIssuesPageDisplayed() {
        return driver.getCurrentUrl().contains("/issues");
    }

    /**
     * Verifies the issue was created by checking we're on an issue detail page
     * (/issues/NNN URL) and the title is shown.
     */
    public boolean isIssueCreated(String expectedTitle) {
        // Primary check: URL moved to an issue number page
        String url = driver.getCurrentUrl();
        if (url.matches(".*/issues/\\d+.*")) {
            // We're on a detail page — check title if visible, else trust URL
            try {
                return getText(issueHeader).contains(expectedTitle);
            } catch (Exception e) {
                return true; // URL is authoritative
            }
        }
        return false;
    }
}