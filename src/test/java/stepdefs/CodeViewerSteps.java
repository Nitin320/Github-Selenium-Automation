package stepdefs;

import driver.DriverFactory;
import driver.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.CodeBrowserPage;
import pages.CommitHistoryPage;
import pages.FileViewPage;
import pages.LoginPage;
import utils.ConfigReader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CodeViewerSteps — Cucumber step definitions backing codeviewer.feature.
 *
 * <p>Covers three scenario groups:
 * <ul>
 *   <li>Folder tree navigation  ({@link CodeBrowserPage})</li>
 *   <li>File content / raw viewing ({@link FileViewPage})</li>
 *   <li>Commit history validation ({@link CommitHistoryPage})</li>
 * </ul>
 *
 * Author: Neil Joe Augustine
 */
public class CodeViewerSteps {

    private CodeBrowserPage  codeBrowserPage;
    private FileViewPage     fileViewPage;
    private CommitHistoryPage commitHistoryPage;

    // ------------------------------------------------------------------ //
    //  Lifecycle                                                           //
    // ------------------------------------------------------------------ //

    @Before
    public void setUp() {
        DriverManager.setDriver(DriverFactory.createDriver());
    }

    @After
    public void tearDown() {
        DriverManager.quitDriver();
    }

    // ------------------------------------------------------------------ //
    //  Background                                                          //
    // ------------------------------------------------------------------ //

    @Given("I am logged in to GitHub")
    public void i_am_logged_in_to_github() {
        LoginPage loginPage = new LoginPage().open();
        loginPage.loginFromConfigAndWaitForLogin();
        assertTrue(loginPage.isLoggedIn(), "Must be logged in before running Code Viewer scenarios");
    }

    // ------------------------------------------------------------------ //
    //  Folder tree navigation                                              //
    // ------------------------------------------------------------------ //

    @When("I open the repository {string}")
    public void i_open_the_repository(String ownerRepo) {
        String[] parts = ownerRepo.split("/", 2);
        codeBrowserPage = new CodeBrowserPage().openRepository(parts[0], parts[1]);
    }

    @And("I click the folder entry {string}")
    public void i_click_the_folder_entry(String name) {
        codeBrowserPage.clickEntry(name);
    }

    @Then("the file tree should be displayed")
    public void the_file_tree_should_be_displayed() {
        assertTrue(codeBrowserPage.isFileTreeVisible(),
            "Expected the repository file tree to be visible");
    }

    @Then("the breadcrumb should contain {string}")
    public void the_breadcrumb_should_contain(String segment) {
        String breadcrumb = codeBrowserPage.getBreadcrumbPath();
        assertTrue(breadcrumb.contains(segment),
            "Expected breadcrumb '" + breadcrumb + "' to contain '" + segment + "'");
    }

    @Then("an entry named {string} should exist in the file tree")
    public void an_entry_named_should_exist_in_the_file_tree(String name) {
        assertTrue(codeBrowserPage.entryExists(name),
            "Expected an entry named '" + name + "' in the file tree");
    }

    // ------------------------------------------------------------------ //
    //  File content / raw viewing                                          //
    // ------------------------------------------------------------------ //

    @When("I open the file {string} in repository {string} on branch {string}")
    public void i_open_the_file_in_repository_on_branch(String file, String ownerRepo, String branch) {
        String[] parts = ownerRepo.split("/", 2);
        fileViewPage = new FileViewPage().openFile(parts[0], parts[1], branch, file);
    }

    @And("I click the Raw button")
    public void i_click_the_raw_button() {
        fileViewPage.viewRaw();
    }

    @Then("the file content should be visible")
    public void the_file_content_should_be_visible() {
        assertTrue(fileViewPage.isFileContentVisible(),
            "Expected the file content (code blob) to be visible");
    }

    @Then("the URL should contain {string}")
    public void the_url_should_contain(String fragment) {
        String url = fileViewPage.getCurrentUrl();
        assertTrue(url.contains(fragment),
            "Expected URL '" + url + "' to contain '" + fragment + "'");
    }

    @Then("the raw file content should not be empty")
    public void the_raw_file_content_should_not_be_empty() {
        String raw = fileViewPage.getRawContent();
        assertFalse(raw.isBlank(), "Expected raw file content to be non-empty");
    }

    // ------------------------------------------------------------------ //
    //  Commit history validation                                           //
    // ------------------------------------------------------------------ //

    @When("I open the commit history for {string} on branch {string}")
    public void i_open_the_commit_history_for_on_branch(String ownerRepo, String branch) {
        String[] parts = ownerRepo.split("/", 2);
        commitHistoryPage = new CommitHistoryPage().openCommits(parts[0], parts[1], branch);
    }

    @And("I click on commit number {int}")
    public void i_click_on_commit_number(int number) {
        // Feature file uses 1-based indexing; convert to 0-based
        commitHistoryPage.clickCommit(number - 1);
    }

    @Then("the commit list should be displayed")
    public void the_commit_list_should_be_displayed() {
        assertTrue(commitHistoryPage.isCommitListVisible(),
            "Expected at least one commit to be visible in the history list");
    }

    @Then("there should be at least {int} commit visible")
    public void there_should_be_at_least_commit_visible(int minCount) {
        int actual = commitHistoryPage.getCommitCount();
        assertTrue(actual >= minCount,
            "Expected at least " + minCount + " commit(s) but found " + actual);
    }

    @Then("each commit message should not be empty")
    public void each_commit_message_should_not_be_empty() {
        commitHistoryPage.getCommitMessages().forEach(msg ->
            assertFalse(msg.isBlank(), "Found a blank commit message in the history list"));
    }

    @Then("the commit detail page should display a SHA")
    public void the_commit_detail_page_should_display_a_sha() {
        String sha = commitHistoryPage.getCommitSha();
        assertFalse(sha.isBlank(), "Expected the commit SHA to be present on the detail page");
    }
}
