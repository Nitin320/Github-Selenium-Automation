package tests;

import driver.DriverFactory;
import driver.DriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import pages.CommitHistoryPage;
import pages.LoginPage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CommitHistoryTest — direct (non-BDD) JUnit 5 coverage for commit history viewing.
 *
 * <p>Logs in once per class ({@code @BeforeAll}) so all tests share a single
 * authenticated browser session — avoids a 120 s login wait per test method.
 *
 * Author: Neil Joe Augustine
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommitHistoryTest {

    private static final String OWNER  = "octocat";
    private static final String REPO   = "Hello-World";
    private static final String BRANCH = "master";

    @BeforeAll
    void loginOnce() {
        DriverManager.setDriver(DriverFactory.createDriver());
        LoginPage loginPage = new LoginPage().open();
        loginPage.loginFromConfigAndWaitForLogin();
        assertTrue(loginPage.isLoggedIn(), "Login must succeed before commit-history tests");
    }

    @AfterAll
    void quitDriver() {
        DriverManager.quitDriver();
    }

    // ------------------------------------------------------------------ //
    //  Tests                                                               //
    // ------------------------------------------------------------------ //

    @Test
    void commitListIsVisibleForKnownRepository() {
        CommitHistoryPage page = new CommitHistoryPage().openCommits(OWNER, REPO, BRANCH);
        assertTrue(page.isCommitListVisible(),
            "Expected the commit history list to be visible for octocat/Hello-World");
    }

    @Test
    void atLeastOneCommitIsPresentInHistory() {
        CommitHistoryPage page = new CommitHistoryPage().openCommits(OWNER, REPO, BRANCH);
        assertTrue(page.getCommitCount() >= 1,
            "Expected at least one commit to be present in the history list");
    }

    @Test
    void allVisibleCommitMessagesAreNonBlank() {
        CommitHistoryPage page = new CommitHistoryPage().openCommits(OWNER, REPO, BRANCH);
        List<String> messages = page.getCommitMessages();
        assertFalse(messages.isEmpty(), "Commit message list must not be empty");
        messages.forEach(msg ->
            assertFalse(msg.isBlank(),
                "Found a blank commit message — each entry must have a non-empty message"));
    }

    @Test
    void commitDetailPageShowsSha() {
        CommitHistoryPage page = new CommitHistoryPage().openCommits(OWNER, REPO, BRANCH);
        page.clickCommit(0);
        String sha = page.getCommitSha();
        assertFalse(sha.isBlank(),
            "Expected the commit SHA to be displayed on the commit detail page");
    }

    @Test
    void commitDetailPageShowsMessageHeading() {
        CommitHistoryPage page = new CommitHistoryPage().openCommits(OWNER, REPO, BRANCH);
        page.clickCommit(0);
        String heading = page.getCommitMessageHeading();
        // Live DOM: h1 = "Commit <short-sha>" — just assert it's non-blank
        assertFalse(heading.isBlank(),
            "Expected the h1 heading to be present on the commit detail page; got blank");
    }
}
