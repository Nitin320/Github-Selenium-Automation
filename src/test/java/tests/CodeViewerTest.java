package tests;

import base.BaseTest;
import org.junit.jupiter.api.Test;
import pages.CodeBrowserPage;
import pages.FileViewPage;
import pages.LoginPage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CodeViewerTest — direct (non-BDD) JUnit 5 coverage for file navigation and viewing.
 *
 * <p>These tests exercise:
 * <ul>
 *   <li>Repository file tree visibility</li>
 *   <li>Entry existence in the file tree</li>
 *   <li>File blob content visibility</li>
 *   <li>Raw button presence and raw URL navigation</li>
 * </ul>
 *
 * <p>Tests run against the public {@code octocat/Hello-World} repository so no
 * repository setup or teardown is required; only login is needed.
 *
 * Author: Neil Joe Augustine
 */
public class CodeViewerTest extends BaseTest {

    private static final String OWNER  = "octocat";
    private static final String REPO   = "Hello-World";
    private static final String BRANCH = "master";
    private static final String FILE   = "README";

    // ------------------------------------------------------------------ //
    //  Helper                                                              //
    // ------------------------------------------------------------------ //

    /** Logs in once using configured credentials and returns the active LoginPage. */
    private LoginPage login() {
        LoginPage loginPage = new LoginPage().open();
        loginPage.loginFromConfigAndWaitForLogin();
        assertTrue(loginPage.isLoggedIn(), "Login must succeed before code-viewer tests");
        return loginPage;
    }

    // ------------------------------------------------------------------ //
    //  File tree tests                                                     //
    // ------------------------------------------------------------------ //

    @Test
    void repositoryFileTreeIsVisible() {
        login();
        CodeBrowserPage page = new CodeBrowserPage().openRepository(OWNER, REPO);
        assertTrue(page.isFileTreeVisible(),
            "File tree table should be visible on the repository root page");
    }

    @Test
    void fileTreeContainsAtLeastOneEntry() {
        login();
        CodeBrowserPage page = new CodeBrowserPage().openRepository(OWNER, REPO);
        assertFalse(page.getEntryNames().isEmpty(),
            "The repository file tree must contain at least one entry");
    }

    @Test
    void readmeEntryExistsInFileTree() {
        login();
        CodeBrowserPage page = new CodeBrowserPage().openRepository(OWNER, REPO);
        assertTrue(page.entryExists("README"),
            "A README entry should be present in the octocat/Hello-World repository");
    }

    // ------------------------------------------------------------------ //
    //  File content tests                                                  //
    // ------------------------------------------------------------------ //

    @Test
    void fileContentIsVisibleInBlobView() {
        login();
        FileViewPage page = new FileViewPage().openFile(OWNER, REPO, BRANCH, FILE);
        assertTrue(page.isFileContentVisible(),
            "File content (code blob) should be visible when opening a known file");
    }

    @Test
    void rawButtonIsVisible() {
        login();
        FileViewPage page = new FileViewPage().openFile(OWNER, REPO, BRANCH, FILE);
        assertTrue(page.isRawButtonVisible(),
            "The Raw button should be present on the file blob page");
    }

    @Test
    void rawViewUrlContainsRawSegment() {
        login();
        FileViewPage page = new FileViewPage().openFile(OWNER, REPO, BRANCH, FILE);
        page.viewRaw();
        String url = page.getCurrentUrl();
        assertTrue(url.contains("/raw/"),
            "After clicking Raw the URL should contain '/raw/'; actual URL: " + url);
    }

    @Test
    void rawContentIsNotEmpty() {
        login();
        FileViewPage page = new FileViewPage().openFile(OWNER, REPO, BRANCH, FILE);
        page.viewRaw();
        assertFalse(page.getRawContent().isBlank(),
            "Raw file content must not be empty for a file that exists");
    }
}
