package tests;

import driver.DriverFactory;
import driver.DriverManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import pages.CodeBrowserPage;
import pages.FileViewPage;
import pages.LoginPage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CodeViewerTest — direct (non-BDD) JUnit 5 coverage for file navigation and viewing.
 *
 * <p>Logs in once per class ({@code @BeforeAll}) so all tests share a single
 * authenticated browser session — avoids a 120 s login wait per test method.
 *
 * Author: Neil Joe Augustine
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CodeViewerTest {

    private static final String OWNER  = "octocat";
    private static final String REPO   = "Hello-World";
    private static final String BRANCH = "master";
    private static final String FILE   = "README";

    @BeforeAll
    void loginOnce() {
        DriverManager.setDriver(DriverFactory.createDriver());
        LoginPage loginPage = new LoginPage().open();
        loginPage.loginFromConfigAndWaitForLogin();
        assertTrue(loginPage.isLoggedIn(), "Login must succeed before code-viewer tests");
    }

    @AfterAll
    void quitDriver() {
        DriverManager.quitDriver();
    }

    // ------------------------------------------------------------------ //
    //  File tree tests                                                     //
    // ------------------------------------------------------------------ //

    @Test
    void repositoryFileTreeIsVisible() {
        CodeBrowserPage page = new CodeBrowserPage().openRepository(OWNER, REPO);
        assertTrue(page.isFileTreeVisible(),
            "File tree table should be visible on the repository root page");
    }

    @Test
    void fileTreeContainsAtLeastOneEntry() {
        CodeBrowserPage page = new CodeBrowserPage().openRepository(OWNER, REPO);
        assertFalse(page.getEntryNames().isEmpty(),
            "The repository file tree must contain at least one entry");
    }

    @Test
    void readmeEntryExistsInFileTree() {
        CodeBrowserPage page = new CodeBrowserPage().openRepository(OWNER, REPO);
        assertTrue(page.entryExists("README"),
            "A README entry should be present in the octocat/Hello-World repository");
    }

    // ------------------------------------------------------------------ //
    //  File content tests                                                  //
    // ------------------------------------------------------------------ //

    @Test
    void fileContentIsVisibleInBlobView() {
        FileViewPage page = new FileViewPage().openFile(OWNER, REPO, BRANCH, FILE);
        assertTrue(page.isFileContentVisible(),
            "File content (code blob) should be visible when opening a known file");
    }

    @Test
    void rawButtonIsVisible() {
        FileViewPage page = new FileViewPage().openFile(OWNER, REPO, BRANCH, FILE);
        assertTrue(page.isRawButtonVisible(),
            "The Raw button should be present on the file blob page");
    }

    @Test
    void rawViewUrlContainsRawSegment() {
        FileViewPage page = new FileViewPage().openFile(OWNER, REPO, BRANCH, FILE);
        page.viewRaw();
        String url = page.getCurrentUrl();
        assertTrue(url.contains("/raw/") || url.contains("raw.githubusercontent.com"),
            "After clicking Raw the URL should be a raw content URL; actual URL: " + url);
    }

    @Test
    void rawContentIsNotEmpty() {
        FileViewPage page = new FileViewPage().openFile(OWNER, REPO, BRANCH, FILE);
        page.viewRaw();
        assertFalse(page.getRawContent().isBlank(),
            "Raw file content must not be empty for a file that exists");
    }
}
