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
        new LoginPage().open().loginFromConfig();

        ProfilePage profilePage = new ProfilePage();
        String expectedUsername = ConfigReader.getProperty("github.username");

        assertEquals(expectedUsername.toLowerCase(), profilePage.getLoggedInUsernameFromHeader().toLowerCase(),
                "Logged-in username in the header should match the account used to sign in");
    }

   /* @Test
    void editProfileButtonIsVisibleOnOwnProfile() {
        new LoginPage().open().loginFromConfig();

        ProfilePage profilePage = new ProfilePage().openViaMenu();

        assertTrue(profilePage.isEditProfileVisible(),
                "The owner of a profile should always see an 'Edit profile' control");
    }*/
}
