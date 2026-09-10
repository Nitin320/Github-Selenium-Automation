package pages;
import org.openqa.selenium.By;
/**

 GistCreatePage — page object for creating and editing GitHub Gists.

 Handles:

 Gist description
 Filename
 File content
 Public/Secret visibility
 Create/Update actions

 Author: Naveen
 */
public class GistCreatePage extends BasePage {

    private final By descriptionField =
            By.name("gist[description]");

    /**

     Filename field used by the Gist editor.
     */
    private final By filenameField =
            By.cssSelector("input[name='gist[files][][name]']");

    /**

     Gist code editor.
     */
    private final By codeEditor =
            By.id("code-editor");

    /**

     Visibility dropdown button.
     */
    private final By visibilityButton =
            By.xpath("//div[@id='new_gist']//button");

    /**

     Visibility menu/summary.
     */
    private final By visibilitySummary =
            By.xpath("//div[@id='new_gist']//details/summary");

    /**

     Public visibility option.
     */
    private final By publicOption =
            By.xpath(
                    "//div[@id='new_gist']//details-menu//label[contains(normalize-space(), 'Public')]"
            );

    /**

     Create Gist button.
     */
    private final By createGistButton =
            By.xpath("//button[contains(normalize-space(), 'Create')]");

    /**

     Update Gist button.
     */
    private final By updateGistButton =
            By.xpath("//button[contains(normalize-space(), 'Update')]");

    /**

     Opens the GitHub Gist homepage.
     */
    public GistCreatePage open() {
        navigateTo("https://gist.github.com/");
        return this;
    }

    /**

     Enters the Gist description.
     */
    public GistCreatePage enterDescription(String description) {
        type(descriptionField, description);
        return this;
    }

    /**

     Enters the Gist filename.
     */
    public GistCreatePage enterFilename(String filename) {
        type(filenameField, filename);
        return this;
    }

    /**

     Enters content into the Gist editor.
     */
    public GistCreatePage enterFileContent(String content) {
        type(codeEditor, content);
        return this;
    }

    /**

     Selects Secret/Hidden visibility.
     GitHub Gists are Secret/Hidden by default.
     */
    public GistCreatePage selectSecret() {
        return this;
    }

    /**

     Changes the Gist visibility to Public.
     */
    public GistCreatePage selectPublic() {
        click(visibilityButton);
        if (isDisplayed(visibilitySummary)) {
            click(visibilitySummary);
        }
        click(publicOption);
        return this;
    }

    /**

     Creates a new Gist.
     */
    public GistViewPage createGist() {
        click(createGistButton);
        return new GistViewPage();
    }

    /**

     Updates an existing Gist.
     */
    public GistViewPage updateGist() {
        click(updateGistButton);
        return new GistViewPage();
    }
}
