package tests;

import base.BaseTest;
import org.junit.jupiter.api.Test;
import pages.LoginPage;
import pages.NewRepoPage;
import pages.RepoHomePage;
import utils.ConfigReader;
import utils.FakerDataFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RepositoryTest — JUnit 5 tests for repository creation and cleanup.
 *
 * Each test creates a uniquely-named repository using FakerDataFactory
 * so runs never collide, and deletes it at the end as cleanup.
 *
 * Author: Sujin
 */
public class RepositoryTest extends BaseTest {

    @Test
    void createPublicRepositoryAndVerifyTitle() {
        new LoginPage().open().loginFromConfigAndWaitForLogin();

        String repoName = FakerDataFactory.getRandomRepoName();
        driver.get(ConfigReader.get("base.url", "https://github.com") + "/new");

        NewRepoPage newRepo = new NewRepoPage();
        newRepo.enterRepoName(repoName).clickCreateRepository();

        RepoHomePage home = new RepoHomePage();
        String title = home.getRepoTitleText();
        assertFalse(title.isBlank(), "Repository title should not be blank after creation");
        assertTrue(title.contains(repoName),
                "Repository title '" + title + "' should contain the created repo name '" + repoName + "'");

        // Cleanup — delete the repository so the account stays clean
        String username = ConfigReader.get("github.username", "");
        driver.get(ConfigReader.get("base.url", "https://github.com")
                + "/" + username + "/" + repoName + "/settings");
        home.deleteRepository(repoName);
    }

    @Test
    void repositorySettingsTabIsAccessible() {
        new LoginPage().open().loginFromConfigAndWaitForLogin();

        String repoName = FakerDataFactory.getRandomRepoName();
        driver.get(ConfigReader.get("base.url", "https://github.com") + "/new");

        new NewRepoPage().enterRepoName(repoName).clickCreateRepository();

        RepoHomePage home = new RepoHomePage();
        home.clickSettings();

        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/settings"),
                "Expected to land on repository settings page but URL was: " + currentUrl);

        // Cleanup
        String username = ConfigReader.get("github.username", "");
        driver.get(ConfigReader.get("base.url", "https://github.com")
                + "/" + username + "/" + repoName + "/settings");
        home.deleteRepository(repoName);
    }
}
