package tests;

import base.BaseTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.ExplorePage;
import pages.SearchPage;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SearchTest — JUnit 5 tests for GitHub Search and Explore.
 *
 * NOTE ON PERFORMANCE / LOAD TESTING:
 * ------------------------------------------------------------------
 * This suite is a *functional* UI automation test suite built on Selenium.
 * Selenium is NOT the right tool for load, stress, or performance testing —
 * it drives a real browser and cannot simulate hundreds of concurrent users.
 *
 * For load/stress/performance testing of GitHub (or any web application) the
 * correct approach is:
 *   • Apache JMeter        — HTTP-level load tests, configurable thread groups
 *   • Gatling              — Scala/Java DSL load testing, rich HTML reports
 *   • k6                   — JavaScript-based, CI-friendly, cloud-scalable
 *   • Locust               — Python-based, easy concurrent user simulation
 *
 * What we CAN do here is a lightweight "response-time guard": assert that
 * a critical page loads within a maximum acceptable wall-clock time.
 * This catches gross regressions (e.g. a search that takes 30 s instead of 3 s)
 * without being a true load test.
 * ------------------------------------------------------------------
 *
 * Author: Yazeen
 */
public class SearchTest extends BaseTest {

    private static final Logger LOG = LoggerFactory.getLogger(SearchTest.class);

    /** Maximum acceptable wall-clock time (ms) for a search results page to appear. */
    private static final long SEARCH_RESPONSE_TIME_LIMIT_MS = 10_000;

    @Test
    void shouldSearchGitHubRepositories() {
        SearchPage searchPage = new SearchPage();

        long startMs = System.currentTimeMillis();
        searchPage.searchFor("selenium");
        long elapsedMs = System.currentTimeMillis() - startMs;

        LOG.info("  ⏱️  Search page load time: {} ms (limit: {} ms)", elapsedMs, SEARCH_RESPONSE_TIME_LIMIT_MS);
        assertTrue(searchPage.isSearchResultsPageDisplayed(),
                "Expected search results page to be displayed");
        assertTrue(searchPage.resultsContain("selenium"),
                "Expected search results to contain 'selenium'");
        assertTrue(elapsedMs < SEARCH_RESPONSE_TIME_LIMIT_MS,
                "Search page took " + elapsedMs + " ms — exceeds the " + SEARCH_RESPONSE_TIME_LIMIT_MS + " ms response-time limit");
    }

    @Test
    void shouldOpenTrendingRepositoriesFromExplore() {
        ExplorePage explorePage = new ExplorePage();
        explorePage.open();

        assertTrue(explorePage.isExplorePageDisplayed(),
                "Expected the GitHub Explore page to be displayed");

        long startMs = System.currentTimeMillis();
        explorePage.openTrendingRepositories();
        long elapsedMs = System.currentTimeMillis() - startMs;

        LOG.info("  ⏱️  Trending page load time: {} ms (limit: {} ms)", elapsedMs, SEARCH_RESPONSE_TIME_LIMIT_MS);
        assertTrue(explorePage.isTrendingPageDisplayed(),
                "Expected the GitHub trending repositories page to be displayed");
        assertTrue(elapsedMs < SEARCH_RESPONSE_TIME_LIMIT_MS,
                "Trending page took " + elapsedMs + " ms — exceeds the " + SEARCH_RESPONSE_TIME_LIMIT_MS + " ms response-time limit");
    }
}
