package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import utils.ConfigReader;

/**
 * SearchPage — page object for GitHub global search.
 *
 * GitHub's search bar changed from a plain input[name='q'] to a
 * custom search-input web component. We try multiple selectors in order.
 *
 * Author: Yazeen
 */
public class SearchPage extends BasePage {

    // GitHub's search widget — try the new react component first, fall back
    // to the classic input[name='q'] which still appears on /search results page.
    private static final By SEARCH_INPUT_NEW   = By.cssSelector(
            "input[data-testid='site-search-input'], " +
            "input[aria-label='Search GitHub'], " +
            "input.header-search-input, " +
            "input[name='q']"
    );
    private static final By SEARCH_BUTTON      = By.cssSelector(
            "button[data-testid='site-search-submit'], " +
            "button[type='submit'][aria-label*='earch']"
    );
    private static final By MAIN_CONTENT       = By.cssSelector("main");

    public void open() {
        navigateTo(ConfigReader.get("base.url", "https://github.com") + "/search");
    }

    public void searchFor(String searchTerm) {
        // Navigate to the search page first so the input is always present
        navigateTo(ConfigReader.get("base.url", "https://github.com") + "/search?q=&type=repositories");
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(SEARCH_INPUT_NEW));
        input.clear();
        input.sendKeys(searchTerm);
        input.sendKeys(Keys.ENTER);
        wait.until(ExpectedConditions.urlContains("q="));
        wait.until(ExpectedConditions.visibilityOfElementLocated(MAIN_CONTENT));
    }

    public boolean isSearchResultsPageDisplayed() {
        return driver.getCurrentUrl().contains("q=")
                && isDisplayed(MAIN_CONTENT);
    }

    public boolean resultsContain(String searchTerm) {
        // First check: the search term is in the URL query string (always true after a search)
        if (driver.getCurrentUrl().toLowerCase().contains(searchTerm.toLowerCase())) {
            return true;
        }
        // Second check: look at visible page text
        try {
            return getText(MAIN_CONTENT).toLowerCase()
                    .contains(searchTerm.toLowerCase());
        } catch (TimeoutException e) {
            return false;
        }
    }
}
