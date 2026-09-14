package tests;

import base.BaseTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.LoginPage;
import pages.ProfilePage;
import utils.ConfigReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ProfileTest — verifies the profile page reflects the logged-in account.
 * Author: Jothi Sri
 */
public class ProfileTest extends BaseTest {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileTest.class);

    @Test
    void profilePageShowsLoggedInUsername() {
        LOG.info("  [1] Opening GitHub login page...");
        LoginPage loginPage = new LoginPage();
        loginPage.open().loginFromConfigAndWaitForLogin();
        LOG.info("  [2] Login successful — account menu is visible.");

        ProfilePage profilePage = new ProfilePage();
        String expectedUsername = ConfigReader.getProperty("github.username");
        LOG.info("  [3] Expected username from config: {}", expectedUsername);

        String actualUsername = profilePage.getLoggedInUsernameFromHeader();
        LOG.info("  [4] Actual username from GitHub header: {}", actualUsername);

        assertEquals(expectedUsername.toLowerCase(), actualUsername.toLowerCase(),
                "Logged-in username in the header should match the account used to sign in");
        LOG.info("  [5] ✅ PASS — username matches.");
    }

    @Test
    void editProfileButtonIsVisibleOnOwnProfile() {
        LOG.info("  [1] Opening GitHub login page...");
        LoginPage loginPage = new LoginPage();
        loginPage.open().loginFromConfigAndWaitForLogin();
        LOG.info("  [2] Login successful — opening profile via account menu...");

        ProfilePage profilePage = new ProfilePage().openViaMenu();
        LOG.info("  [3] Profile page opened. URL: {}", driver.getCurrentUrl());

        boolean editProfileVisible = profilePage.isEditProfileVisible();
        LOG.info("  [4] 'Edit profile' button visible: {}", editProfileVisible);

        assertTrue(editProfileVisible,
                "The owner of a profile should always see an 'Edit profile' control");
        LOG.info("  [5] ✅ PASS — 'Edit profile' button is visible.");
    }
}