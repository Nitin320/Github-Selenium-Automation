package stepdefs;

import driver.DriverManager;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.NewRepoPage;
import pages.RepoHomePage;
import utils.ConfigReader;
import utils.FakerDataFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RepositorySteps — step definitions for repository creation and navigation.
 *
 * Driver lifecycle is managed centrally by {@link Hooks}.
 * Author: Sujin
 *
 * NOTE: The "the user is logged into GitHub" step is defined in GistSteps
 * (shared login step). It is NOT duplicated here to avoid
 * Cucumber's AmbiguousStepDefinitionsException.
 */
public class RepositorySteps {

    private static final Logger LOG = LoggerFactory.getLogger(RepositorySteps.class);

    private NewRepoPage  newRepoPage;
    private RepoHomePage repoHomePage;

    /** Runtime-generated repo name so each test run uses a fresh, unique repo. */
    private String createdRepoName;

    private NewRepoPage newRepoPage() {
        if (newRepoPage == null) newRepoPage = new NewRepoPage();
        return newRepoPage;
    }

    private RepoHomePage repoHomePage() {
        if (repoHomePage == null) repoHomePage = new RepoHomePage();
        return repoHomePage;
    }

    @When("the user navigates to the new repository page")
    public void the_user_navigates_to_the_new_repository_page() {
        String baseUrl = ConfigReader.get("base.url", "https://github.com");
        DriverManager.getDriver().get(baseUrl + "/new");
        LOG.info("  ➡️  Navigated to new repository page");
    }

    @When("the user creates a new repository with a unique name")
    public void the_user_creates_a_new_repository_with_a_unique_name() {
        createdRepoName = FakerDataFactory.getRandomRepoName();
        LOG.info("  ✏️  Creating repository: {}", createdRepoName);
        newRepoPage().enterRepoName(createdRepoName);
        newRepoPage().clickCreateRepository();
    }

    @Then("the repository should be created and visible")
    public void the_repository_should_be_created_and_visible() {
        String title = repoHomePage().getRepoTitleText();
        LOG.info("  🔍  Repository title on page: {}", title);
        assertFalse(title.isBlank(),
            "Repository title should not be blank after creation");
        assertTrue(title.contains(createdRepoName),
            "Expected repository title to contain '" + createdRepoName
            + "' but was: '" + title + "'");
        LOG.info("  ✅  Repository '{}' created and visible", createdRepoName);
    }

    @Then("the repository settings tab should be accessible")
    public void the_repository_settings_tab_should_be_accessible() {
        repoHomePage().clickSettings();
        String url = DriverManager.getDriver().getCurrentUrl();
        assertTrue(url.contains("/settings"),
            "Expected to land on the settings page but URL was: " + url);
        LOG.info("  ✅  Settings page accessible at: {}", url);
    }

    @And("the user deletes the repository to clean up")
    public void the_user_deletes_the_repository_to_clean_up() {
        if (createdRepoName == null) {
            LOG.warn("  ⚠️  No repository name recorded — skipping cleanup");
            return;
        }
        String username = ConfigReader.get("github.username", "");
        String deleteUrl = ConfigReader.get("base.url", "https://github.com")
                + "/" + username + "/" + createdRepoName + "/settings";
        LOG.info("  🗑️  Navigating to settings for cleanup: {}", deleteUrl);
        DriverManager.getDriver().get(deleteUrl);
        repoHomePage().deleteRepository(createdRepoName);
        LOG.info("  ✅  Repository '{}' deleted (cleanup complete)", createdRepoName);
    }

    // ── Legacy step kept for backward compatibility ──────────────────────────

    @When("the user enters repository name {string} and clicks create")
    public void the_user_enters_repository_name_and_clicks_create(String repoName) {
        createdRepoName = repoName;
        newRepoPage().enterRepoName(repoName);
        newRepoPage().clickCreateRepository();
    }

    @Then("the repository should be successfully created")
    public void the_repository_should_be_successfully_created() {
        the_repository_should_be_created_and_visible();
    }

    @Then("the user deletes the repository for cleanup")
    public void the_user_deletes_the_repository_for_cleanup() {
        the_user_deletes_the_repository_to_clean_up();
    }
}
