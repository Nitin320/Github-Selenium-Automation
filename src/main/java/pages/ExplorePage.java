package pages;

import org.openqa.selenium.By;
import utils.ConfigReader;

/**
 * ExplorePage — page object for GitHub Explore.
 * Author: Yazeen
 */
public class ExplorePage extends BasePage {
    private static final By MAIN_CONTENT = By.cssSelector("main");

    public void open() {
        navigateTo(ConfigReader.get("base.url", "https://github.com") + "/explore");
    }

    public void openTrendingRepositories() {
        navigateTo(ConfigReader.get("base.url", "https://github.com") + "/trending");
        waitForVisibility(MAIN_CONTENT);
    }

    public boolean isExplorePageDisplayed() {
        return driver.getCurrentUrl().contains("/explore")
                && isDisplayed(MAIN_CONTENT);
    }

    public boolean isTrendingPageDisplayed() {
        return driver.getCurrentUrl().contains("/trending")
                && isDisplayed(MAIN_CONTENT);
    }


}
