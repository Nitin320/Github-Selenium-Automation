package stepdefs;

import driver.DriverFactory;
import driver.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.ExplorePage;
import pages.SearchPage;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SearchSteps — step definitions for GitHub Search and Explore.
 * Author: Yazeen
 */
public class SearchSteps {

    private SearchPage searchPage;
    private ExplorePage explorePage;

    @Before
    public void setUp() {
        DriverManager.setDriver(DriverFactory.createDriver());
        searchPage = new SearchPage();
        explorePage = new ExplorePage();
    }

    @After
    public void tearDown() {
        DriverManager.quitDriver();
    }

    @Given("I am on the GitHub search page")
    public void openSearchPage() {
        searchPage.open();
    }

    @When("I search for {string}")
    public void searchFor(String searchTerm) {
        searchPage.searchFor(searchTerm);
    }

    @Then("the GitHub search results page is displayed")
    public void verifySearchResultsPage() {
        assertTrue(searchPage.isSearchResultsPageDisplayed());
    }

    @Then("the search results contain {string}")
    public void verifySearchResults(String searchTerm) {
        assertTrue(searchPage.resultsContain(searchTerm));
    }

    @Given("I am on the GitHub Explore page")
    public void openExplorePage() {
        explorePage.open();
    }

    @When("I open trending repositories")
    public void openTrendingRepositories() {
        explorePage.openTrendingRepositories();
    }

    @Then("the GitHub Explore page is displayed")
    public void verifyExplorePage() {
        assertTrue(explorePage.isExplorePageDisplayed());
    }

    @Then("the GitHub trending page is displayed")
    public void verifyTrendingPage() {
        assertTrue(explorePage.isTrendingPageDisplayed());
    }
}
