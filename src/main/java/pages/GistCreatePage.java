package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * GistCreatePage — page object for creating and editing GitHub Gists.
 *
 * Handles:
 * - Gist description
 * - Filename
 * - File content
 * - Public/Secret visibility
 * - Create/Update actions
 *
 * Author: Naveen
 */
public class GistCreatePage extends BasePage {

    private final By descriptionField =
            By.name("gist[description]");

    /*
     * Filename input. GitHub renders this as:
     *   <input type="text" ... placeholder="Filename including extension…"
     *          name="gist[files][][name]" id="gist_filename_0" ...>
     * Both the name attribute and the id are reliable selectors.
     */
    private final By filenameField =
            By.xpath("//input[contains(@placeholder,'Filename') or @id='gist_filename_0']");

    /*
     * The visible CodeMirror editor div — this is what receives clicks and
     * keyboard input. The hidden <textarea> behind it does NOT accept sendKeys.
     */
    private final By codeEditorContainer =
            By.id("code-editor");

    /*
     * Secret/Create button — submits the form as a Secret gist.
     * This is the primary submit button present by default on the create form.
     * Matches: //*[@id="new_gist"]/div/div[2]/div/button
     */
    private final By secretCreateButton =
            By.xpath("//*[@id=\"new_gist\"]/div/div[2]/div/button");

    /*
     * Visibility dropdown summary — opens the public/secret menu.
     * Matches: //*[@id="new_gist"]/div/div[2]/div/details/summary
     */
    private final By visibilitySummary =
            By.xpath("//*[@id=\"new_gist\"]/div/div[2]/div/details/summary");

    /*
     * Public option inside the open visibility menu.
     * Matches: //*[@id="new_gist"]/div/div[2]/div/details/details-menu/label[2]
     */
    private final By publicOption =
            By.xpath(
                    "//*[@id=\"new_gist\"]/div/div[2]/div/details/details-menu/label[2]"
            );

    /*
     * Create/Update button — present after selecting Public visibility.
     * Also doubles as the Update button on the edit form.
     */
    private final By createGistButton =
            By.xpath("//button[contains(normalize-space(), 'Create')]");

    /*
     * Update button on the edit form.
     */
    private final By updateGistButton =
            By.xpath("//button[contains(normalize-space(), 'Update')]");

    /**
     * Enters Gist description.
     */
    public GistCreatePage enterDescription(String description) {
        type(descriptionField, description);
        return this;
    }

    /**
     * Enters filename.
     */
    public GistCreatePage enterFilename(String filename) {
        type(filenameField, filename);
        return this;
    }

    /**
     * Enters file content into the CodeMirror editor.
     *
     * CodeMirror renders a hidden backing <textarea> that does not accept
     * sendKeys. The interactive surface is the editor container div itself:
     * click it to acquire focus, select-all to clear any existing content,
     * then type the new content.
     */
    public GistCreatePage enterFileContent(String content) {
        WebElement editor = waitForClickable(codeEditorContainer);
        editor.click();
        // Clear any pre-existing content (e.g. when editing an existing Gist)
      //  editor.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        editor.sendKeys(content);
        return this;
    }

    /**
     * Selects Secret visibility.
     * The secret/create button is the default submit on the new-gist form —
     * clicking it creates the gist as Secret without opening any dropdown.
     * Do NOT call createGist() after this; this method submits the form itself.
     *
     * Matches raw: driver.findElement(By.xpath(
     *   "//*[@id=\"new_gist\"]/div/div[2]/div/button")).click();
     */
    public GistViewPage selectSecretAndCreate() {
        click(secretCreateButton);
        return new GistViewPage();
    }

    /**
     * Selects Secret visibility without submitting (used on the edit form
     * where the dropdown still exists but the button label differs).
     */
    public GistCreatePage selectSecret() {
        // Gists are Secret by default — no action needed on a fresh create form.
        // On the edit form the visibility cannot be changed so this is a no-op.
        return this;
    }

    /**
     * Changes the Gist visibility to Public.
     * Opens the details/summary dropdown then clicks the Public label.
     * Matches raw: click summary → click label[2]
     */
    public GistCreatePage selectPublic() {
        click(visibilitySummary);
        click(publicOption);
        return this;
    }

    /**
     * Creates a new Gist.
     */
    public GistViewPage createGist() {

        click(createGistButton);

        return new GistViewPage();
    }

    /**
     * Updates an existing Gist.
     */
    public GistViewPage updateGist() {

        click(updateGistButton);

        return new GistViewPage();
    }
}
