package tests;

import base.BaseTest;
import org.junit.jupiter.api.Test;
import pages.LoginPage;
import utils.ConfigReader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LoginTest — direct (non-BDD) JUnit 5 coverage for the login flow.
 * Author: Jothi Sri
 */
public class LoginTest extends BaseTest {

    @Test
    void validLoginSucceeds() {
        LoginPage loginPage = new LoginPage();
        loginPage.open().loginFromConfigAndWaitForLogin();
        assertTrue(loginPage.isLoggedIn(), "Expected the account menu to be visible after a valid login. " + "Instead landed on: " + loginPage.getCurrentPageInfo());
    }

    @Test
    void invalidPasswordShowsError() {
        LoginPage loginPage = new LoginPage();
        String validUsername = ConfigReader.getProperty("github.username");
        loginPage.open().login(validUsername, "WrongPassword123!");
        assertTrue(loginPage.isLoginErrorDisplayed(), "Expected an error message for an invalid password");
        assertFalse(loginPage.isLoggedIn(), "User must not be logged in with an invalid password");
    }

    @Test
    void emptyPasswordDoesNotLogIn() {
        LoginPage loginPage = new LoginPage();
        loginPage.open().login(ConfigReader.getProperty("github.username"), "");
        assertFalse(loginPage.isLoggedIn(), "An empty password must never result in an authenticated session");
    }
}
