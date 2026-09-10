package tests;

import base.BaseTest;
import org.junit.jupiter.api.Test;
import pages.ExplorePage;
import pages.SearchPage;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SearchTest — JUnit 5 tests for GitHub Search and Explore.
 * Author: Yazeen
 */
public class SearchTest extends BaseTest {

    @Test
    void shouldSearchGitHubRepositories() {
        SearchPage searchPage = new SearchPage();
        searchPage.searchFor("selenium");

        assertTrue(searchPage.isSearchResultsPageDisplayed());
        assertTrue(searchPage.resultsContain("selenium"));
    }

    @Test
    void shouldOpenTrendingRepositoriesFromExplore() {
        ExplorePage explorePage = new ExplorePage();
        explorePage.open();

        assertTrue(explorePage.isExplorePageDisplayed());

        explorePage.openTrendingRepositories();
        assertTrue(explorePage.isTrendingPageDisplayed());
    }
}
