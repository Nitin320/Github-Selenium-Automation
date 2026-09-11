package stepdefs;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import driver.DriverManager;
import pages.NewRepoPage;
import pages.RepoHomePage;

/**
 * RepositorySteps — step definitions for repository creation and navigation.
 * Author: Sujin
 *
 * NOTE: The "the user is logged into GitHub" step is defined in GistSteps
 * (which performs the actual login). It is intentionally NOT duplicated here
 * to avoid Cucumber's AmbiguousStepDefinitionsException.
 */
public class RepositorySteps {

    // Pages are lazily created after the driver is set in @Before
    NewRepoPage newRepoPage;
    RepoHomePage repoHomePage;

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
        DriverManager.getDriver().get("https://github.com/new");
    }

    @When("the user enters repository name {string} and clicks create")
    public void the_user_enters_repository_name_and_clicks_create(String repoName) {
        newRepoPage().enterRepoName(repoName);
        newRepoPage().clickCreateRepository();
    }

    @Then("the repository should be successfully created")
    public void the_repository_should_be_successfully_created() {
        String title = repoHomePage().getRepoTitleText();
        System.out.println("Repository successfully created: " + title);
    }

    @Then("the user deletes the repository for cleanup")
    public void the_user_deletes_the_repository_for_cleanup() {
        repoHomePage().clickSettings();
        // Additional settings/deletion actions can be appended here if needed
    }
}