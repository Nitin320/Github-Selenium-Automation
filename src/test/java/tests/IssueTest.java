package tests;

import base.BaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import pages.IssuePage;
import utils.ConfigReader;

/**
 * IssueTest — JUnit 5 tests for GitHub Issues.
 * Author: Deva Vignan
 */
public class IssueTest extends BaseTest {

    @Test
    void issuesPageCanBeOpenedWhenConfigured() {
        String username = ConfigReader.get("github.username", "");
        String repository = ConfigReader.get("test.repo", "");
        Assumptions.assumeTrue(!username.isBlank() && !repository.isBlank(),
                "Configure github.username and test.repo for the GitHub UI smoke test");

        new IssuePage();
        driver.get(ConfigReader.get("base.url", "https://github.com")
                + "/" + username + "/" + repository + "/issues");

        Assertions.assertTrue(driver.getCurrentUrl().contains("/issues"));
    }
}
