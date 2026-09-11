/**
 * GistTest — JUnit 5 tests for GitHub Gist creation.
 * Author: Naveen
 */

package tests;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import base.BaseTest;
import pages.GistCreatePage;
import pages.GistListPage;
import pages.GistViewPage;
import pages.LoginPage;

/**
 * GistTest — direct JUnit 5 coverage for GitHub Gist functionality.
 *
 * Covers:
 *   Create public Gist
 *   Create secret Gist
 *   Edit public Gist
 *   Edit secret Gist
 *   Delete public Gist
 *   Delete secret Gist
 *
 * Author: Naveen
 */
public class GistTest extends BaseTest {

    /**
     * Tracks gist names created during create/edit tests so they can be
     * cleaned up in @AfterEach, preventing orphaned gists in the account.
     */
    private final List<String> gistsToDelete = new ArrayList<>();

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        new LoginPage().open().loginFromConfigAndWaitForLogin();
    }

    @AfterEach
    public void deleteOrphanedGists() {
        if (gistsToDelete.isEmpty()) {
            return;
        }
        GistListPage listPage = new GistListPage().open();
        for (String gistName : gistsToDelete) {
            if (listPage.isGistPresent(gistName)) {
                listPage.openGist(gistName)
                        .clickDelete()
                        .confirmDelete();
                // Return to list for the next iteration
                listPage = new GistListPage().open();
            }
        }
    }

    @Test
    void createPublicGist() {

        String description = "Public Gist Test";
        String filename = "public-test.txt";
        String content = "This is a public Gist created for Selenium.";

        GistListPage gistListPage = new GistListPage();
        GistCreatePage gistCreatePage = gistListPage.open().clickNewGist();

        GistViewPage gistViewPage = gistCreatePage
                .enterDescription(description)
                .enterFilename(filename)
                .enterFileContent(content)
                .selectPublic()
                .createGist();

        assertTrue(
                gistViewPage.isGistDisplayed(),
                "Expected the public Gist to be displayed after creation"
        );
        assertTrue(
                gistViewPage.isContentDisplayed(content),
                "Expected the created Gist to contain the supplied content"
        );
        assertTrue(
                gistViewPage.isPublic(),
                "Expected the created Gist to be public"
        );

        gistsToDelete.add(gistViewPage.getGistName());
    }

    @Test
    void createSecretGist() {

        String description = "Secret Gist Test";
        String filename = "secret-test.txt";
        String content = "This is a secret Gist created by Selenium.";

        GistListPage gistListPage = new GistListPage();
        GistCreatePage gistCreatePage = gistListPage.open().clickNewGist();

        GistViewPage gistViewPage = gistCreatePage
                .enterDescription(description)
                .enterFilename(filename)
                .enterFileContent(content)
                .selectSecretAndCreate();

        assertTrue(
                gistViewPage.isGistDisplayed(),
                "Expected the secret Gist to be displayed after creation"
        );
        assertTrue(
                gistViewPage.isContentDisplayed(content),
                "Expected the created Gist to contain the supplied content"
        );
        assertTrue(
                gistViewPage.isSecret(),
                "Expected the created Gist to be secret"
        );

        gistsToDelete.add(gistViewPage.getGistName());
    }

    @Test
    void editPublicGist() {

        String description = "Editable Public Gist";
        String filename = "edit-public.txt";
        String originalContent = "Original public Gist content.";
        String updatedContent = "Updated public Gist content.";

        GistViewPage gistViewPage = new GistListPage()
                .open()
                .clickNewGist()
                .enterDescription(description)
                .enterFilename(filename)
                .enterFileContent(originalContent)
                .selectPublic()
                .createGist();

        assertTrue(
                gistViewPage.isPublic(),
                "Expected the Gist to be public before editing"
        );

        GistViewPage updatedGist = gistViewPage
                .clickEdit()
                .enterFileContent(updatedContent)
                .updateGist();

        assertTrue(
                updatedGist.isContentDisplayed(updatedContent),
                "Expected the updated public Gist content to be displayed"
        );
        assertTrue(
                updatedGist.isEditButtonDisplayed(),
                "Expected to stay on the updated Gist page where the Edit button is present"
        );

        gistsToDelete.add(updatedGist.getGistName());
    }

    @Test
    void editSecretGist() {

        String description = "Editable Secret Gist";
        String filename = "edit-secret.txt";
        String originalContent = "Original secret Gist content.";
        String updatedContent = "Updated secret Gist content.";

        GistViewPage gistViewPage = new GistListPage()
                .open()
                .clickNewGist()
                .enterDescription(description)
                .enterFilename(filename)
                .enterFileContent(originalContent)
                .selectSecretAndCreate();

        assertTrue(
                gistViewPage.isSecret(),
                "Expected the Gist to be secret before editing"
        );

        GistViewPage updatedGist = gistViewPage
                .clickEdit()
                .enterFileContent(updatedContent)
                .updateGist();

        assertTrue(
                updatedGist.isContentDisplayed(updatedContent),
                "Expected the updated secret Gist content to be displayed"
        );
        assertTrue(
                updatedGist.isEditButtonDisplayed(),
                "Expected to stay on the updated Gist page where the Edit button is present"
        );

        gistsToDelete.add(updatedGist.getGistName());
    }

    @Test
    void deletePublicGist() {

        String description = "Delete Public Gist";
        String filename = "delete-public.txt";
        String content = "Public Gist to be deleted.";

        GistViewPage gistViewPage = new GistListPage()
                .open()
                .clickNewGist()
                .enterDescription(description)
                .enterFilename(filename)
                .enterFileContent(content)
                .selectPublic()
                .createGist();

        assertTrue(
                gistViewPage.isPublic(),
                "Expected the Gist to be public before deletion"
        );

        String gistName = gistViewPage.getGistName();

        gistViewPage
                .clickDelete()
                .confirmDelete();

        // After deletion, GitHub automatically redirects to the Gist list page
        GistListPage gistListPage = new GistListPage();

        assertFalse(
                gistListPage.isGistPresent(gistName),
                "Expected the Gist to no longer exist after deletion"
        );
    }

    @Test
    void deleteSecretGist() {

        String description = "Delete Secret Gist";
        String filename = "delete-secret.txt";
        String content = "Secret Gist to be deleted.";

        GistViewPage gistViewPage = new GistListPage()
                .open()
                .clickNewGist()
                .enterDescription(description)
                .enterFilename(filename)
                .enterFileContent(content)
                .selectSecretAndCreate();

        assertTrue(
                gistViewPage.isSecret(),
                "Expected the Gist to be secret before deletion"
        );

        String gistName = gistViewPage.getGistName();

        gistViewPage
                .clickDelete()
                .confirmDelete();

        // After deletion, GitHub automatically redirects to the Gist list page
        GistListPage gistListPage = new GistListPage();

        assertFalse(
                gistListPage.isGistPresent(gistName),
                "Expected the secret Gist to no longer exist after deletion"
        );
    }
}
