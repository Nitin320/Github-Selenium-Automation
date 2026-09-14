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
    // GitHub's sign-out is now a form submission button. Try multiple selectors.
    // Confirmed from screenshot: the menu shows a plain list item "→ Sign out"
    // rendered as a <button> or <a> inside the slide-out panel.
    // There is NO <form action="/logout"> wrapper in the new GitHub UI.
    // The button text is exactly "Sign out" (capital S, space, lowercase o-u-t).
    private final By signOutButton = By.xpath(
            "//button[normalize-space()='Sign out'] | "
            + "//a[normalize-space()='Sign out'] | "
            + "//button[.//span[normalize-space()='Sign out']] | "
            + "//a[.//span[normalize-space()='Sign out']]"
    );
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

    /**
     * Signs out of the current session.
     *
     * <p>GitHub's header shows a round avatar button (confirmed from screenshot).
     * Clicking it opens a slide-out panel.  "Sign out" appears as a plain
     * {@code <button>} at the bottom of that panel with an icon and the text
     * "Sign out".  We click the avatar, wait for the item to appear, then use
     * JavaScript to click it (avoids any stale element / intercepted click issues
     * caused by the slide animation still running).
     */
    public void signOut() {
        // 1. Open the user menu
        click(accountMenuButton);

        // 2. Wait up to 15 s for "Sign out" to become visible (menu has a slide animation)
        org.openqa.selenium.support.ui.WebDriverWait menuWait =
                new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(15));
        org.openqa.selenium.WebElement btn = menuWait.until(
                org.openqa.selenium.support.ui.ExpectedConditions
                        .visibilityOfElementLocated(signOutButton));

        // 3. JS-click to avoid interception by the sliding animation
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);

        // 4. Wait until the avatar/account button disappears (session ended)
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions
                .invisibilityOfElementLocated(By.cssSelector("button[data-login]")));
    }
}
