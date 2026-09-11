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

    /*
     * Gist name/identifier.
     */
    private final By gistName =
            By.cssSelector(".css-truncate-target.mr-1");

    /*
     * Edit button.
     */
    private final By editButton =
            By.xpath(
                    "//*[@id='gist-pjax-container']//a[contains(normalize-space(), 'Edit')]"
            );

    /*
     * Delete button.
     */
    private final By deleteButton =
            By.xpath(
                    "//*[@id='gist-pjax-container']//form//button[contains(normalize-space(), 'Delete')]"
            );

    /*
     * File content — all rendered lines of the gist.
     */
    private final By fileContent =
            By.cssSelector(".blob-code-inner");

    /*
     * Public indicator.
     */
    private final By publicIndicator =
            By.xpath(
                    "//*[@id='gist-pjax-container']//*[contains(normalize-space(), 'Public')]"
            );

    /*
     * Secret/Hidden indicator.
     */
    private final By secretIndicator =
            By.xpath(
                    "//*[@id='gist-pjax-container']//*[contains(normalize-space(), 'Secret') " +
                            "or contains(normalize-space(), 'Hidden')]"
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
     * Checks whether Gist is public.
     */
    public boolean isPublic() {
        return isDisplayed(publicIndicator);
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
     * Deletes the Gist.
     */
    public GistViewPage clickDelete() {

        click(deleteButton);

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
