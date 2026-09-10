package pages;

import org.openqa.selenium.By;

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

    private final By filenameField =
            By.cssSelector("input[name='gist[files][][name]']");

    private final By codeEditor =
            By.id("code-editor");

    /*
     * Visibility dropdown.
     */
    private final By visibilityButton =
            By.xpath("//*[@id='new_gist']//button");

    /*
     * Visibility menu.
     */
    private final By visibilitySummary =
            By.xpath("//*[@id='new_gist']//details/summary");

    /*
     * Public option.
     */
    private final By publicOption =
            By.xpath(
                    "//*[@id='new_gist']//details-menu//label[contains(normalize-space(), 'Public')]"
            );

    /*
     * Create button.
     */
    private final By createGistButton =
            By.xpath("//button[contains(normalize-space(), 'Create')]");

    /*
     * Update button.
     */
    private final By updateGistButton =
            By.xpath("//button[contains(normalize-space(), 'Update')]");

    /**
     * Opens the GitHub Gist homepage.
     */
    public GistCreatePage open() {
        navigateTo("https://gist.github.com/");
        return this;
    }

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
     * Enters file content.
     */
    public GistCreatePage enterFileContent(String content) {
        type(codeEditor, content);
        return this;
    }

    /**
     * GitHub Gists are Secret/Hidden by default.
     */
    public GistCreatePage selectSecret() {
        return this;
    }

    /**
     * Changes the Gist visibility to Public.
     */
    public GistCreatePage selectPublic() {

        click(visibilityButton);

        /*
         * If the details menu is present, open it.
         */
        try {
            if (isDisplayed(visibilitySummary)) {
                click(visibilitySummary);
            }
        } catch (Exception ignored) {
            // Menu may already be open.
        }

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
