package tests;

import base.BaseTest;
import org.junit.jupiter.api.Test;
import pages.LoginPage;
import pages.ProfilePage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LogoutTest — verifies a session can be terminated cleanly via the account menu.
 * Author: Jothi Sri
 */
public class LogoutTest extends BaseTest {

    @Test
    void logoutEndsTheSession() {
        LoginPage loginPage = new LoginPage();
        loginPage.open().loginFromConfig();
        assertTrue(loginPage.isLoggedIn(), "Precondition failed: must be logged in before testing logout");

        ProfilePage profilePage = new ProfilePage();
        profilePage.signOut();

        assertFalse(loginPage.isLoggedIn(),
                "Account menu should no longer be visible once the user has signed out");
    }
}
