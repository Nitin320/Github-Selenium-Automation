package tests;

import base.BaseTest;
import org.junit.jupiter.api.Test;
import pages.CommitHistoryPage;
import pages.LoginPage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CommitHistoryTest — direct (non-BDD) JUnit 5 coverage for commit history viewing.
 *
 * <p>These tests exercise:
 * <ul>
 *   <li>Commit list visibility on a known public repository</li>
 *   <li>At least one commit present in the list</li>
 *   <li>All visible commit messages are non-blank</li>
 *   <li>Clicking into a commit detail page and reading the SHA</li>
 * </ul>
 *
 * <p>Tests run against the public {@code octocat/Hello-World} repository so no
 * repository setup or teardown is required; only login is needed.
 *
 * Author: Neil Joe Augustine
 */
public class CommitHistoryTest extends BaseTest {

    private static final String OWNER  = "octocat";
    private static final String REPO   = "Hello-World";
    private static final String BRANCH = "master";

    // ------------------------------------------------------------------ //
    //  Helper                                                              //
    // ------------------------------------------------------------------ //

    private void login() {
        LoginPage loginPage = new LoginPage().open();
        loginPage.loginFromConfigAndWaitForLogin();
        assertTrue(loginPage.isLoggedIn(), "Login must succeed before commit-history tests");
    }

    // ------------------------------------------------------------------ //
    //  Tests                                                               //
    // ------------------------------------------------------------------ //

    @Test
    void commitListIsVisibleForKnownRepository() {
        login();
        CommitHistoryPage page = new CommitHistoryPage().openCommits(OWNER, REPO, BRANCH);
        assertTrue(page.isCommitListVisible(),
            "Expected the commit history list to be visible for octocat/Hello-World");
    }

    @Test
    void atLeastOneCommitIsPresentInHistory() {
        login();
        CommitHistoryPage page = new CommitHistoryPage().openCommits(OWNER, REPO, BRANCH);
        assertTrue(page.getCommitCount() >= 1,
            "Expected at least one commit to be present in the history list");
    }

    @Test
    void allVisibleCommitMessagesAreNonBlank() {
        login();
        CommitHistoryPage page = new CommitHistoryPage().openCommits(OWNER, REPO, BRANCH);
        List<String> messages = page.getCommitMessages();
        assertFalse(messages.isEmpty(), "Commit message list must not be empty");
        messages.forEach(msg ->
            assertFalse(msg.isBlank(),
                "Found a blank commit message — each entry must have a non-empty message"));
    }

    @Test
    void commitDetailPageShowsSha() {
        login();
        CommitHistoryPage page = new CommitHistoryPage().openCommits(OWNER, REPO, BRANCH);
        page.clickCommit(0);
        String sha = page.getCommitSha();
        assertFalse(sha.isBlank(),
            "Expected the commit SHA to be displayed on the commit detail page");
    }

    @Test
    void commitDetailPageShowsMessageHeading() {
        login();
        CommitHistoryPage page = new CommitHistoryPage().openCommits(OWNER, REPO, BRANCH);
        String firstMessage = page.getCommitMessages().get(0);
        page.clickCommit(0);
        String heading = page.getCommitMessageHeading();
        assertFalse(heading.isBlank(),
            "Expected a commit message heading to be displayed on the detail page");
        assertTrue(heading.contains(firstMessage) || firstMessage.contains(heading),
            "Commit detail heading '" + heading + "' should match list message '" + firstMessage + "'");
    }
}
