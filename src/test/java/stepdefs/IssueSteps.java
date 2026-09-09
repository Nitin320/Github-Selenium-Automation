package stepdefs;

import driver.DriverFactory;
import driver.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import pages.IssuePage;
import utils.ConfigReader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * IssueSteps — step definitions for GitHub Issues scenarios.
 * Author: Deva Vignan
 */
public class IssueSteps {

    private WebDriver driver;
    private IssuePage issuePage;
    private String issuesUrl;
    private String issueTitle;

    @Before
    public void setUp() {
        DriverManager.setDriver(DriverFactory.createDriver());
        driver = DriverManager.getDriver();
        issuePage = new IssuePage();
    }

    @Given("a GitHub repository is configured")
    public void repositoryIsConfigured() {
        String username = ConfigReader.get("github.username", "");
        String repository = ConfigReader.get("test.repo", "");
        assertFalse(username.isBlank(), "github.username is not configured");
        assertFalse(repository.isBlank(), "test.repo is not configured");

        issuesUrl = ConfigReader.get("base.url", "https://github.com")
                + "/" + username + "/" + repository + "/issues";
    }

    @When("I open the repository issues page")
    public void openRepositoryIssuesPage() {
        driver.get(issuesUrl);
    }

    @Then("the issues page should be displayed")
    public void issuesPageShouldBeDisplayed() {
        assertTrue(issuePage.isIssuesPageDisplayed(),
                "The repository issues page was not displayed");
    }

    @When("I create an issue with title {string}")
    public void createIssueWithTitle(String title) {
        issueTitle = title;
        issuePage.clickNewIssue();
        issuePage.enterIssueTitle(title);
    }

    @When("I enter the issue description {string}")
    public void enterIssueDescription(String description) {
        issuePage.enterIssueDescription(description);
    }

    @When("I submit the issue")
    public void submitIssue() {
        issuePage.submitIssue();
    }

    @Then("the issue should be created successfully")
    public void issueShouldBeCreatedSuccessfully() {
        assertTrue(issuePage.isIssueCreated(issueTitle),
                "The created issue title was not displayed");
    }

    @After
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
