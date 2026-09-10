package pages;

import org.openqa.selenium.By;

/**
 * NewRepoPage — page object for GitHub's new repository creation form.
 * Author: Sujin
 */
public class NewRepoPage extends BasePage {

    private final By repoNameInput    = By.id("repository_name");
    private final By createRepoButton = By.cssSelector("button[data-target='new-repository.submitButton'], button[type='submit']");

    public NewRepoPage enterRepoName(String name) {
        type(repoNameInput, name);
        return this;
    }

    public NewRepoPage clickCreateRepository() {
        click(createRepoButton);
        return this;
    }
}
