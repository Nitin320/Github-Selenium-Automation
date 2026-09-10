package pages;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.TimeoutException;

/**

 GistViewPage — page object for viewing, editing and deleting

 an existing GitHub Gist.

 Author: Naveen
 */
public class GistViewPage extends BasePage {

    /**

     Gist identifier/name displayed in the Gist header.
     */
    private final By gistName =
            By.cssSelector(".css-truncate-target.mr-1");

    /**

     Edit button.
     **/
     private final By editButton =
     By.xpath(
     "//[@id='gist-pjax-container']//a[contains(normalize-space(), 'Edit')]"
     );

     /**

     Delete button.
     **/
     private final By deleteButton =
     By.xpath(
     "//[@id='gist-pjax-container']//form//button[contains(normalize-space(), 'Delete')]"
     );

     /**

     File content displayed on the Gist view page.
     */
    private final By fileContent =
            By.cssSelector(".blob-code-inner");

    /**

     Public visibility indicator.
     **/
     private final By publicIndicator =
     By.xpath(
     "//[@id='gist-pjax-container']//*[contains(normalize-space(), 'Public')]"
     );

     /**

     Secret/Hidden visibility indicator.
     **/
     private final By secretIndicator =
     By.xpath(
     "//[@id='gist-pjax-container']//*[contains(normalize-space(), 'Secret') " +
     "or contains(normalize-space(), 'Hidden')]"
     );

     /**

     Flash message displayed after operations such as deletion.
     */
    private final By flashMessage =
            By.cssSelector(
                    "#js-flash-container [role='alert'], " +
                            "#js-flash-container .flash"
            );

    /**

     Gets the displayed Gist name.
     */
    public String getGistName() {
        return getText(gistName);
    }

    /**

     Checks whether the Gist is displayed.
     */
    public boolean isGistDisplayed() {
        return isDisplayed(gistName);
    }

    /**

     Gets the visible file content.
     */
    public String getFileContent() {
        return getText(fileContent);
    }

    /**

     Checks whether the supplied content is displayed.
     */
    public boolean isContentDisplayed(String content) {

        try {
            return getFileContent().contains(content);
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**

     Checks whether the current Gist is public.
     */
    public boolean isPublic() {
        return isDisplayed(publicIndicator);
    }

    /**

     Checks whether the current Gist is Secret/Hidden.
     */
    public boolean isSecret() {
        return isDisplayed(secretIndicator);
    }

    /**

     Clicks the Edit button.

     The GitHub edit form uses the same editor represented

     by GistCreatePage.
     */
    public GistCreatePage clickEdit() {

        click(editButton);

        return new GistCreatePage();
    }

    /**

     Clicks the Delete button.
     */
    public GistViewPage clickDelete() {

        click(deleteButton);

        return this;
    }

    /**

     Confirms deletion if GitHub displays a browser alert.

     Some GitHub UI versions may use an in-page confirmation

     instead of a browser alert.
     */
    public GistViewPage confirmDelete() {

        try {
            driver.switchTo().alert().accept();
        } catch (NoAlertPresentException e) {
            /*
             * No browser alert was displayed.
             *
             * If the current GitHub UI uses an in-page confirmation,
             * that confirmation should be handled with its locator.
             */
        }

        return this;
    }

    /**

     Checks whether a deletion/status flash message is displayed.
     */
    public boolean isFlashMessageDisplayed() {
        return isDisplayed(flashMessage);
    }

    /**

     Gets the deletion/status flash message.
     */
    public String getFlashMessage() {
        return getText(flashMessage);
    }
}
