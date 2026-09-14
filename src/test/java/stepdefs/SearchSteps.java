package stepdefs;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.ExplorePage;
import pages.SearchPage;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SearchSteps — step definitions for GitHub Search and Explore.
 *
 * Driver lifecycle is managed centrally by {@link Hooks}.
 * Author: Yazeen
 */
public class SearchSteps {

    // Page objects lazily created after Hooks sets the driver
    private SearchPage searchPage() { return new SearchPage(); }
    private ExplorePage explorePage() { return new ExplorePage(); }

    @Given("I am on the GitHub search page")
    public void openSearchPage() {
        searchPage().open();
    }

    @When("I search for {string}")
    public void searchFor(String searchTerm) {
        searchPage().searchFor(searchTerm);
    }

    @Then("the GitHub search results page is displayed")
    public void verifySearchResultsPage() {
        assertTrue(searchPage().isSearchResultsPageDisplayed(),
                "Expected the GitHub search results page to be displayed");
    }

    @Then("the search results contain {string}")
    public void verifySearchResults(String searchTerm) {
        assertTrue(searchPage().resultsContain(searchTerm),
                "Expected search results to contain: " + searchTerm);
    }

    @Given("I am on the GitHub Explore page")
    public void openExplorePage() {
        explorePage().open();
    }

    @When("I open trending repositories")
    public void openTrendingRepositories() {
        explorePage().openTrendingRepositories();
    }

    @Then("the GitHub Explore page is displayed")
    public void verifyExplorePage() {
        assertTrue(explorePage().isExplorePageDisplayed(),
                "Expected the GitHub Explore page to be displayed");
    }

    @Then("the GitHub trending page is displayed")
    public void verifyTrendingPage() {
        assertTrue(explorePage().isTrendingPageDisplayed(),
                "Expected the GitHub trending repositories page to be displayed");
    }
}
