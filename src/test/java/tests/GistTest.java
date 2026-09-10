/**
 * GistTest — JUnit 5 tests for GitHub Gist creation.
 * Author: Naveen
 */

package tests;

import base.BaseTest;
import org.junit.jupiter.api.Test;
import pages.GistCreatePage;
import pages.GistListPage;
import pages.GistViewPage;
import pages.LoginPage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**

 GistTest — direct JUnit 5 coverage for GitHub Gist functionality.

 Covers:

 Create public Gist
 Create secret Gist
 Edit public Gist
 Edit secret Gist
 Delete public Gist
 Delete secret Gist

 Author: Naveen
 */
public class GistTest extends BaseTest {

    @Test
    void createPublicGist() {

        LoginPage loginPage = new LoginPage();
        loginPage.open().loginFromConfigAndWaitForLogin();

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


    }

    @Test
    void createSecretGist() {

        LoginPage loginPage = new LoginPage();
        loginPage.open().loginFromConfigAndWaitForLogin();

        String description = "Secret Gist Test";
        String filename = "secret-test.txt";
        String content = "This is a secret Gist created by Selenium.";

        GistListPage gistListPage = new GistListPage();
        GistCreatePage gistCreatePage = gistListPage.open().clickNewGist();

        GistViewPage gistViewPage = gistCreatePage
                .enterDescription(description)
                .enterFilename(filename)
                .enterFileContent(content)
                .selectSecret()
                .createGist();

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


    }

    @Test
    void editPublicGist() {

        LoginPage loginPage = new LoginPage();
        loginPage.open().loginFromConfigAndWaitForLogin();

        String description = "Editable Public Gist";
        String filename = "edit-public.txt";
        String originalContent = "Original public Gist content.";
        String updatedContent = "Updated public Gist content.";

        GistCreatePage gistCreatePage =
                new GistListPage()
                        .open()
                        .clickNewGist();

        GistViewPage gistViewPage = gistCreatePage
                .enterDescription(description)
                .enterFilename(filename)
                .enterFileContent(originalContent)
                .selectPublic()
                .createGist();

        assertTrue(
                gistViewPage.isPublic(),
                "Expected the Gist to be public before editing"
        );

        GistCreatePage editPage = gistViewPage.clickEdit();

        GistViewPage updatedGist = editPage
                .enterFileContent(updatedContent)
                .updateGist();

        assertTrue(
                updatedGist.isContentDisplayed(updatedContent),
                "Expected the updated public Gist content to be displayed"
        );


    }

    @Test
    void editSecretGist() {

        LoginPage loginPage = new LoginPage();
        loginPage.open().loginFromConfigAndWaitForLogin();

        String description = "Editable Secret Gist";
        String filename = "edit-secret.txt";
        String originalContent = "Original secret Gist content.";
        String updatedContent = "Updated secret Gist content.";

        GistCreatePage gistCreatePage =
                new GistListPage()
                        .open()
                        .clickNewGist();

        GistViewPage gistViewPage = gistCreatePage
                .enterDescription(description)
                .enterFilename(filename)
                .enterFileContent(originalContent)
                .selectSecret()
                .createGist();

        assertTrue(
                gistViewPage.isSecret(),
                "Expected the Gist to be secret before editing"
        );

        GistCreatePage editPage = gistViewPage.clickEdit();

        GistViewPage updatedGist = editPage.enterFileContent(updatedContent).updateGist();

        assertTrue(updatedGist.isContentDisplayed(updatedContent),
                "Expected the updated secret Gist content to be displayed"
        );


    }

    @Test
    void deletePublicGist() {

        LoginPage loginPage = new LoginPage();
        loginPage.open().loginFromConfigAndWaitForLogin();

        String description = "Delete Public Gist";
        String filename = "delete-public.txt";
        String content = "Public Gist to be deleted.";

        GistViewPage gistViewPage =
                new GistListPage()
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

        GistListPage gistListPage =
                new GistListPage().open();

        assertFalse(
                gistListPage.isGistPresent(gistName),
                "Expected the Gist to no longer exist after deletion"
        );


    }

    @Test
    void deleteSecretGist() {

        LoginPage loginPage = new LoginPage();
        loginPage.open().loginFromConfigAndWaitForLogin();

        String description = "Delete Secret Gist";
        String filename = "delete-secret.txt";
        String content = "Secret Gist to be deleted.";

        GistViewPage gistViewPage = new GistListPage().open().clickNewGist().enterDescription(description).enterFilename(filename).enterFileContent(content).selectSecret().createGist();

        assertTrue(gistViewPage.isSecret(),"Expected the Gist to be secret before deletion"
        );
        String gistName = gistViewPage.getGistName();
        gistViewPage.clickDelete().confirmDelete();
        GistListPage gistListPage = new GistListPage().open();
        assertFalse(gistListPage.isGistPresent(gistName),"Expected the secret Gist to no longer exist after deletion"
        );
    }
}
