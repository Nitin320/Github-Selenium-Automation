package tests;

import base.BaseTest;
import org.junit.jupiter.api.Test;
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

    @Test
    void profilePageShowsLoggedInUsername() {
        System.out.println("PROFILE TEST - Verify Logged-in Username");
        System.out.println("[1] Opening GitHub login page...");
        LoginPage loginPage = new LoginPage();
        loginPage.open().loginFromConfigAndWaitForLogin();
        System.out.println("[2] Login successful.");
        System.out.println("[3] Account menu is visible.");
        ProfilePage profilePage = new ProfilePage();
        String expectedUsername = ConfigReader.getProperty("github.username");
        System.out.println("[4] Expected username: " + expectedUsername);
        String actualUsername = profilePage.getLoggedInUsernameFromHeader();
        System.out.println("[5] Username displayed in GitHub header: " + actualUsername);
        System.out.println("[6] Comparing expected username with actual username...");
        assertEquals(expectedUsername.toLowerCase(), actualUsername.toLowerCase(), "Logged-in username in the header should match the account used to sign in");
        System.out.println("[7] PASS - Logged-in username matches the configured account.");
    }


    @Test
    void editProfileButtonIsVisibleOnOwnProfile() {
        System.out.println("PROFILE TEST - Verify Edit Profile");
        System.out.println("[1] Opening GitHub login page...");
        LoginPage loginPage = new LoginPage();
        loginPage.open().loginFromConfigAndWaitForLogin();
        System.out.println("[2] Login successful.");
        System.out.println("[3] Opening account menu and selecting 'Your profile'...");
        ProfilePage profilePage = new ProfilePage().openViaMenu();
        System.out.println("[4] Own profile page opened.");
        System.out.println("[5] Current URL: " + driver.getCurrentUrl());
        System.out.println("[6] Checking whether 'Edit profile' button is visible...");
        boolean editProfileVisible = profilePage.isEditProfileVisible();
        System.out.println("[7] Edit profile button visible: " + editProfileVisible);
        assertTrue(editProfileVisible, "The owner of a profile should always see an 'Edit profile' control");
        System.out.println("[8] PASS - Edit profile button is visible.");
    }
}