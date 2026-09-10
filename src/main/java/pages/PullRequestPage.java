package pages;

import org.openqa.selenium.By;

/**
 * PullRequestPage — page object for GitHub Pull Requests tab.
 * Author: Deva Vignan
 */
public class PullRequestPage extends BasePage {

    private final By pullRequestsTab =
            By.xpath("//a[contains(@href,'/pulls')] | //span[normalize-space()='Pull requests']");

    private final By newPullRequestButton =
            By.xpath("//a[contains(normalize-space(.),'New pull request')]"
                    + " | //button[contains(normalize-space(.),'New pull request')]");

    private final By pullRequestTitle =
            By.cssSelector("input[name='pull_request[title]'], #pull_request_title");

    private final By pullRequestDescription =
            By.cssSelector("textarea[name='pull_request[body]'], textarea");

    private final By createPullRequestButton =
            By.xpath("//button[contains(normalize-space(.),'Create pull request')]"
                    + " | //input[@value='Create pull request']");

    private final By pullRequestHeader =
            By.xpath("//h1[contains(normalize-space(.),'#')]");

    public void clickPullRequestsTab() {
        click(pullRequestsTab);
    }

    public void clickNewPullRequest() {
        click(newPullRequestButton);
    }

    public void enterPullRequestTitle(String title) {
        type(pullRequestTitle, title);
    }

    public void enterPullRequestDescription(String description) {
        type(pullRequestDescription, description);
    }

    public void createPullRequest() {
        click(createPullRequestButton);
    }

    public boolean isPullRequestCreated() {
        return isDisplayed(pullRequestHeader);
    }

    public String getPullRequestHeader() {
        return getText(pullRequestHeader);
    }
}
