package pages;

import org.openqa.selenium.By;

/**
 * RepoHomePage — page object for a GitHub repository's home page.
 * Author: Sujin
 */
public class RepoHomePage extends BasePage {

    private final By repoTitleHeader = By.cssSelector("strong[itemprop='name'] a");
    private final By settingsTab     = By.id("settings-tab");

    public String getRepoTitleText() {
        return getText(repoTitleHeader);
    }

    public void clickSettings() {
        click(settingsTab);
    }
}