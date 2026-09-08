package stepdefs;

import driver.DriverFactory;
import driver.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pages.LoginPage;
import pages.ProfilePage;
import utils.ConfigReader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LoginSteps — Cucumber step definitions backing login.feature.
 * Author: Jothi Sri
 */
public class LoginSteps {

    private LoginPage loginPage;
    private ProfilePage profilePage;

    @Before
    public void setUp() {
        DriverManager.setDriver(DriverFactory.createDriver());
    }

    @After
    public void tearDown() {
        DriverManager.quitDriver();
    }

    @Given("I am on the GitHub login page")
    public void i_am_on_the_github_login_page() {
        loginPage = new LoginPage().open();
    }

    @Given("I have logged in with valid credentials")
    public void i_have_logged_in_with_valid_credentials() {
        loginPage = new LoginPage().open();
        loginPage.loginFromConfigAndWaitForLogin();
        assertTrue(loginPage.isLoggedIn(), "Setup login must succeed before this scenario can proceed");
    }

    @When("I log in with valid credentials")
    public void i_log_in_with_valid_credentials() {
        loginPage.loginFromConfigAndWaitForLogin();
    }

    @When("I log in with a valid username and password {string}")
    public void i_log_in_with_a_valid_username_and_password(String password) {
        String username = ConfigReader.getProperty("github.username");
        loginPage.login(username, password);
    }

    @When("I log in with a valid username and an empty password")
    public void i_log_in_with_a_valid_username_and_an_empty_password() {
        String username =ConfigReader.getProperty("github.username");
        loginPage.login(username, "");
    }

    @When("I sign out from my account")
    public void i_sign_out_from_my_account() {
        profilePage = new ProfilePage();
        profilePage.signOut();
    }

    @Then("I should be redirected to my GitHub dashboard")
    public void i_should_be_redirected_to_my_github_dashboard() {
        assertTrue(DriverManager.getDriver().getCurrentUrl().contains("github.com"));
    }

    @Then("the account menu should be visible")
    public void the_account_menu_should_be_visible() {
        assertTrue(loginPage.isLoggedIn(), "Account menu should be visible after successful login");
    }

    @Then("the account menu should not be visible")
    public void the_account_menu_should_not_be_visible() {
        assertFalse(loginPage.isLoggedIn(), "Account menu should not be visible when login fails");
    }

    @Then("the account menu should no longer be visible")
    public void the_account_menu_should_no_longer_be_visible() {
        assertFalse(loginPage.isLoggedIn(), "Account menu should no longer be visible after logout");
    }

    @Then("I should see a login error message")
    public void i_should_see_a_login_error_message() {
        assertTrue(loginPage.isLoginErrorDisplayed(), "A login error message should be displayed");
    }
}