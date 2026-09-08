package pages;

import org.openqa.selenium.By;
import utils.ConfigReader;

/**
 * ProfilePage — page object for a GitHub user's profile page and the
 * account-menu actions (view profile, sign out).
 * Author: Jothi Sri
 */
public class ProfilePage extends BasePage {

    private final By accountMenuButton = By.cssSelector("button[data-login], img[data-testid='github-avatar']");
    private final By accountMenuButtonOnly = By.cssSelector("button[data-login]");

    // GitHub's menu markup (classes/tags) changed along with the header
    // redesign. Instead of guessing new class names, these match on things
    // that don't change with a redesign: the actual profile URL, and the
    // visible text a screen reader / real user would rely on.
    private final By yourProfileLink = By.xpath("//a[@href='/" + ConfigReader.getProperty("github.username") + "'] | " + "//a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'your profile')]");
    private final By signOutButton = By.xpath("//button[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign out')] | " + "//a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'sign out')]");
    private final By profileNameHeading = By.cssSelector("[itemprop='name']");
    private final By profileUsername    = By.cssSelector(".p-nickname.vcard-username");
    private final By profileBio         = By.cssSelector(".p-note.user-profile-bio div");
    private final By editProfileButton = By.xpath("//button[normalize-space()='Edit profile']");
    /** Opens the logged-in user's profile via the header account menu. */
    public ProfilePage openViaMenu() {
        click(accountMenuButton);
        click(yourProfileLink);
        return this;
    }

    /** Opens a profile directly by username (works whether or not you're logged in). */
    public ProfilePage openDirect(String username) {
        navigateTo("https://github.com/" + username);
        return this;
    }

    public String getDisplayName() {
        return getText(profileNameHeading);
    }

    public String getUsername() {
        return getText(profileUsername).replace("@", "").trim();
    }

    /**
     * Reads the logged-in username straight from the header's data-login
     * attribute. More reliable than scraping the profile page body, since
     * that markup is unverified against GitHub's current redesign — this
     * attribute was confirmed directly from DevTools.
     */
    public String getLoggedInUsernameFromHeader() {
        return waitForVisibility(accountMenuButtonOnly).getAttribute("data-login");
    }

    public boolean isBioDisplayed() {
        return isDisplayed(profileBio);
    }

    public boolean isEditProfileVisible() {
        return isDisplayed(editProfileButton);
    }

    /** Signs out of the current session via the account menu. */
    public void signOut() {
        click(accountMenuButton);
        click(signOutButton);
    }
}
