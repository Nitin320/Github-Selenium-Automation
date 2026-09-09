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

    private final By newIssueButton =
            By.xpath("//span[contains(text(),'New issue')]");

    private final By issueTitle =
            By.id("issue_title");

    private final By issueDescription =
            By.xpath("//textarea");

    private final By submitIssueButton =
            By.xpath("//button[contains(@class,'btn-primary')]");

    private final By issueHeader =
            By.xpath("//h1");

    private final By issuesPageHeader =
            By.xpath("//h1[normalize-space()='Issues']");

    // Methods

    public void clickIssuesTab() {
        click(issuesTab);
    }

    public void clickNewIssue() {
        click(newIssueButton);
    }

    public void enterIssueTitle(String title) {
        type(issueTitle, title);
    }

    public void enterIssueDescription(String description) {
        type(issueDescription, description);
    }

    public void submitIssue() {
        click(submitIssueButton);
    }

    public boolean isIssuesPageDisplayed() {
        return isDisplayed(issuesPageHeader);
    }

    public boolean isIssueCreated(String expectedTitle) {
        return isDisplayed(issueHeader)
                && getText(issueHeader).contains(expectedTitle);
    }
}