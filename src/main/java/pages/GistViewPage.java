package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;

/**
 * GistViewPage — page object for viewing, editing and deleting
 * an existing GitHub Gist.
 *
 * Author: Naveen
 */
public class GistViewPage extends BasePage {

    /*
     * Gist name/identifier — the truncated link in the header.
     * Matches: .css-truncate-target.mr-1
     * Raw: driver.findElement(By.cssSelector(".css-truncate-target.mr-1")).getText()
     */
    private final By gistName =
            By.cssSelector(".css-truncate-target.mr-1");

    /*
     * Edit button.
     * Raw: //*[@id="gist-pjax-container"]/div[1]/div/div[1]/ul[2]/li[1]/...
     * Using href match which is stable regardless of list position.
     */
    private final By editButton =
            By.xpath(
                    "//*[@id=\"gist-pjax-container\"]/div[1]/div/div[1]/ul[2]/li[1]/a"
            );

    /*
     * Delete button — exact path from working raw code:
     * //*[@id="gist-pjax-container"]/div[1]/div/div[1]/ul[2]/li[2]/form/button
     */
    private final By deleteButton =
            By.xpath("//*[@id=\"gist-pjax-container\"]/div[1]/div/div[1]/ul[2]/li[2]/form/button");
    /*
     * File content — all rendered lines of the gist.
     */
    private final By fileContent =
            By.cssSelector(".blob-code-inner");

    /*
     * Secret/Hidden indicator.
     * GitHub only renders a Label badge for Secret gists — public gists have NO badge.
     * Actual HTML: <span class="Label v-align-middle">Secret</span>
     */
    private final By secretIndicator =
            By.xpath(
                    "//*[@id='gist-pjax-container']//span[contains(@class,'Label') " +
                    "and (normalize-space()='Secret' or normalize-space()='Hidden')]"
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
     * Gets the Gist name.
     */
    public String getGistName() {
        return getText(gistName);
    }

    /**
     * Checks whether the Gist is displayed.
     */
    public boolean isGistDisplayed() {
        return isDisplayed(gistName);
    }

    /**
     * Gets the full visible file content by concatenating all rendered lines.
     */
    public String getFileContent() {
        try {
            return wait.until(
                    org.openqa.selenium.support.ui.ExpectedConditions
                            .presenceOfAllElementsLocatedBy(fileContent)
            ).stream()
                    .map(el -> el.getText())
                    .collect(java.util.stream.Collectors.joining("\n"));
        } catch (TimeoutException e) {
            return "";
        }
    }

    /**
     * Checks whether supplied content is displayed across all file lines.
     */
    public boolean isContentDisplayed(String content) {
        return getFileContent().contains(content);
    }

    /**
     * A public Gist has NO Secret/Hidden badge — GitHub only shows a badge for Secret.
     * So isPublic() == true when the Secret badge is absent.
     */
    public boolean isPublic() {
        return !isDisplayed(secretIndicator);
    }

    /**
     * Checks whether Gist is Secret/Hidden.
     */
    public boolean isSecret() {
        return isDisplayed(secretIndicator);
    }

    /**
     * Opens the edit page.
     */
    public GistCreatePage clickEdit() {

        click(editButton);

        return new GistCreatePage();
    }

    /**
     * Clicks the Delete button on the gist view page.
     * GitHub may show a confirmation dialog after this click.
     */
    public GistViewPage clickDelete() {

        click(deleteButton);

        return this;
    }

    /**
     * Confirms the delete.
     * GitHub's delete button carries data-confirm="..." which triggers a native
     * browser confirm() dialog. Accept it to proceed with deletion.
     */
    public GistViewPage confirmDelete() {

        try {
            driver.switchTo().alert().accept();
        } catch (org.openqa.selenium.NoAlertPresentException e) {
            // No alert — deletion was already committed.
        }

        return this;
    }

    /**
     * Checks whether a flash message is displayed.
     * Useful for verifying success/error feedback after create, edit, or delete.
     */
    public boolean isFlashMessageDisplayed() {
        return isDisplayed(flashMessage);
    }

    /**
     * Gets the flash message text.
     * Useful for verifying the specific message shown after create, edit, or delete.
     */
    public String getFlashMessage() {
        return getText(flashMessage);
    }
}
