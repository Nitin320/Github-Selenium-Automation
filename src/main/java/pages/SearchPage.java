package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import utils.ConfigReader;

/**
 * SearchPage — page object for GitHub global search.
 * Author: Yazeen
 */
public class SearchPage extends BasePage {
    private static final By SEARCH_INPUT = By.cssSelector("input[name='q']");
    private static final By MAIN_CONTENT = By.cssSelector("main");

    public void open() {
        navigateTo(ConfigReader.get("base.url", "https://github.com") + "/search");
    }

    public void searchFor(String searchTerm) {
        open();
        type(SEARCH_INPUT, searchTerm);
        waitForClickable(SEARCH_INPUT).sendKeys(Keys.ENTER);
        wait.until(ExpectedConditions.urlContains("/search"));
    }

    public boolean isSearchResultsPageDisplayed() {
        return driver.getCurrentUrl().contains("/search")
                && isDisplayed(MAIN_CONTENT);
    }

    public boolean resultsContain(String searchTerm) {
        try {
            return getText(MAIN_CONTENT).toLowerCase()
                    .contains(searchTerm.toLowerCase());
        } catch (TimeoutException e) {
            return false;
        }
    }

}
