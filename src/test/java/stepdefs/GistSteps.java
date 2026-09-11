package stepdefs;

import driver.DriverFactory;
import driver.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import pages.GistCreatePage;
import pages.GistListPage;
import pages.GistViewPage;
import pages.LoginPage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GistSteps — Cucumber step definitions for GitHub Gist operations.
 *
 * Author: Naveen
 */
public class GistSteps {

    private GistListPage gistListPage;
    private GistCreatePage gistCreatePage;
    private GistViewPage gistViewPage;

    private String expectedUpdatedContent;
    private String deletedGistName;

    @Before
    public void setUp() {
        DriverManager.setDriver(DriverFactory.createDriver());
    }

    @After
    public void tearDown() {
        DriverManager.quitDriver();
    }

    /**
     * Logs the user into GitHub using configured credentials.
     */
    @Given("the user is logged into GitHub")
    public void the_user_is_logged_into_github() {

        LoginPage loginPage = new LoginPage();

        loginPage
                .open()
                .loginFromConfigAndWaitForLogin();

        assertTrue(
                loginPage.isLoggedIn(),
                "Expected the user to be logged into GitHub"
        );
    }

    /**

     Opens the New Gist creation form.
     */
    @Given("the user navigates to the Gist creation page")
    public void the_user_navigates_to_the_gist_creation_page() {

        gistListPage = new GistListPage();

        gistCreatePage = gistListPage
                .open()
                .clickNewGist();
    }

    /**

     Creates a public Gist.
     */
    @When("the user creates a public gist with filename {string} and content {string}")
    public void the_user_creates_a_public_gist(
            String filename,
            String content) {

        gistViewPage = gistCreatePage
                .enterFilename(filename)
                .enterFileContent(content)
                .selectPublic()
                .createGist();
    }

    /**

     Creates a secret Gist.

     GitHub Gists are Secret/Hidden by default.
     */
    @When("the user creates a secret gist with filename {string} and content {string}")
    public void the_user_creates_a_secret_gist(
            String filename,
            String content) {

        gistViewPage = gistCreatePage
                .enterFilename(filename)
                .enterFileContent(content)
                .selectSecret()
                .createGist();
    }

    /**

     Verifies that the Gist was created.
     */
    @Then("the gist should be created successfully")
    public void the_gist_should_be_created_successfully() {

        assertTrue(
                gistViewPage.isGistDisplayed(),
                "Expected the created Gist to be displayed"
        );
    }

    /**

     Verifies that the supplied Gist content is displayed.
     */
    @Then("the gist content should contain {string}")
    public void the_gist_content_should_contain(String content) {

        assertTrue(
                gistViewPage.isContentDisplayed(content),
                "Expected Gist to contain: " + content
        );
    }

    /**

     Verifies that the Gist is public.
     */
    @Then("the gist should be public")
    public void the_gist_should_be_public() {

        assertTrue(
                gistViewPage.isPublic(),
                "Expected the Gist to be public"
        );
    }

    /**

     Verifies that the Gist is secret.
     */
    @Then("the gist should be secret")
    public void the_gist_should_be_secret() {

        assertTrue(
                gistViewPage.isSecret(),
                "Expected the Gist to be Secret/Hidden"
        );
    }

    /**

     Edits the currently opened Gist.
     */
    @When("the user edits the gist with content {string}")
    public void the_user_edits_the_gist_with_content(
            String updatedContent) {

        expectedUpdatedContent = updatedContent;

        gistCreatePage = gistViewPage.clickEdit();

        gistViewPage = gistCreatePage
                .enterFileContent(updatedContent)
                .updateGist();
    }

    /**

     Verifies the updated Gist content.
     */
    @Then("the updated gist content should be displayed")
    public void the_updated_gist_content_should_be_displayed() {

        assertTrue(
                gistViewPage.isContentDisplayed(expectedUpdatedContent),
                "Expected updated Gist content to be displayed: "
                        + expectedUpdatedContent
        );
        assertTrue(
                gistViewPage.isEditButtonDisplayed(),
                "Expected to stay on the updated Gist page where the Edit button is present"
        );
    }

    /**

     Deletes the currently opened Gist.
     */
    @When("the user deletes the gist")
    public void the_user_deletes_the_gist() {

/*

Save the name before deletion.
*/
        deletedGistName = gistViewPage.getGistName();

        gistViewPage
                .clickDelete()
                .confirmDelete();
    }

    /**

     Verifies that the Gist was deleted.
     */
    @Then("the gist should be deleted successfully")
    public void the_gist_should_be_deleted_successfully() {

        // Stay on the redirected page (Gist list with flash message) after delete
        gistListPage = new GistListPage();

        assertFalse(
                gistListPage.isGistPresent(deletedGistName),
                "Expected the deleted Gist to no longer exist: "
                        + deletedGistName
        );
    }
}
