package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * GistCreatePage — page object for creating and editing GitHub Gists.
 *
 * Handles:
 * - Gist description
 * - Filename
 * - File content (CodeMirror 5 and CodeMirror 6 / cm-editor)
 * - Public/Secret visibility
 * - Create/Update actions
 *
 * Author: Naveen
 */
public class GistCreatePage extends BasePage {

    // ── Locators ──────────────────────────────────────────────────────────────

    private final By descriptionField = By.name("gist[description]");

    // The filename input on gist.github.com is inside a CodeMirror-based editor.
    // GitHub uses a plain <input> for the filename but it is rendered inside a
    // custom web component that takes a moment to attach after navigation.
    private final By filenameField = By.cssSelector(
            "input[name='gist[files][][name]'], " +
            "input.js-gist-filename, " +
            "input[placeholder*='Filename']"
    );

    // Used only as a presence sentinel to know the editor has mounted.
    // We do NOT click this — we use JS to set the value instead.
    private final By codeMirrorContainer = By.cssSelector(".CodeMirror, .cm-editor");

    // Visibility — GitHub now renders this as a <details> element.
    private final By visibilitySummary = By.cssSelector(
            "details.select-menu summary, " +
            "button[aria-label*='isibility'], " +
            "summary[aria-haspopup]"
    );
    private final By publicOption = By.xpath(
            "//label[contains(normalize-space(), 'Public')] | " +
            "//*[contains(@class,'select-menu-item') and contains(normalize-space(),'Public')]"
    );

    private final By createGistButton = By.xpath(
            "//button[contains(normalize-space(), 'Create public gist') or " +
            "contains(normalize-space(), 'Create secret gist') or " +
            "contains(normalize-space(), 'Create gist')]"
    );
    private final By updateGistButton = By.xpath(
            "//button[contains(normalize-space(), 'Update')]"
    );

    // ── Navigation ────────────────────────────────────────────────────────────

    public GistCreatePage open() {
        navigateTo("https://gist.github.com/");
        return this;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    public GistCreatePage enterDescription(String description) {
        type(descriptionField, description);
        return this;
    }

    /**
     * Enters the gist filename.
     * Waits up to 20 s for the input to be interactable — the CodeMirror
     * editor and filename widget initialise asynchronously after page load.
     */
    public GistCreatePage enterFilename(String filename) {
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement input = longWait.until(ExpectedConditions.elementToBeClickable(filenameField));
        input.clear();
        input.sendKeys(filename);
        return this;
    }

    /**
     * Sets the file content in the CodeMirror editor.
     *
     * <p>GitHub Gist uses CodeMirror 5 (classic) or CodeMirror 6 depending on
     * the browser session.  Neither accepts plain {@code sendKeys} reliably
     * because the editor manages its own virtual DOM / input model.
     *
     * <p>Strategy (tried in order):
     * <ol>
     *   <li><b>CodeMirror 5 JS API</b> — {@code .CodeMirror} element exposes a
     *       {@code .CodeMirror} JS property whose {@code setValue()} is the
     *       canonical way to set content programmatically.</li>
     *   <li><b>CodeMirror 6 JS API</b> — {@code .cm-editor} exposes a
     *       {@code .CodeMirror} or {@code .view} property with a transaction
     *       dispatch API.</li>
     *   <li><b>Hidden textarea fallback</b> — CodeMirror mirrors every change
     *       into a hidden {@code <textarea>}; setting its value + dispatching
     *       an {@code input} event forces the editor to sync.</li>
     * </ol>
     */
    public GistCreatePage enterFileContent(String content) {
        // Wait for the editor container to exist before touching JS
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.presenceOfElementLocated(codeMirrorContainer));

        JavascriptExecutor js = (JavascriptExecutor) driver;

        // ── Strategy 1: CodeMirror 5 API ─────────────────────────────────────
        Boolean cm5Set = (Boolean) js.executeScript(
            "try {" +
            "  var cms = document.querySelectorAll('.CodeMirror');" +
            "  if (cms.length > 0 && cms[0].CodeMirror) {" +
            "    cms[0].CodeMirror.setValue(arguments[0]);" +
            "    cms[0].CodeMirror.focus();" +
            "    return true;" +
            "  }" +
            "  return false;" +
            "} catch(e) { return false; }",
            content);

        if (Boolean.TRUE.equals(cm5Set)) {
            return this;
        }

        // ── Strategy 2: CodeMirror 6 dispatch API ────────────────────────────
        Boolean cm6Set = (Boolean) js.executeScript(
            "try {" +
            "  var editors = document.querySelectorAll('.cm-editor');" +
            "  for (var i = 0; i < editors.length; i++) {" +
            "    var view = editors[i].CodeMirror || editors[i]._cmView;" +
            "    if (view && view.dispatch) {" +
            "      var tr = view.state.update({" +
            "        changes: {from:0, to:view.state.doc.length, insert: arguments[0]}" +
            "      });" +
            "      view.dispatch(tr);" +
            "      view.focus();" +
            "      return true;" +
            "    }" +
            "  }" +
            "  return false;" +
            "} catch(e) { return false; }",
            content);

        if (Boolean.TRUE.equals(cm6Set)) {
            return this;
        }

        // ── Strategy 3: hidden textarea + input event ─────────────────────────
        js.executeScript(
            "var ta = document.querySelector('.CodeMirror textarea, textarea[name*=\"gist\"]');" +
            "if (ta) {" +
            "  var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, 'value').set;" +
            "  nativeSetter.call(ta, arguments[0]);" +
            "  ta.dispatchEvent(new Event('input', {bubbles:true}));" +
            "  ta.dispatchEvent(new Event('change', {bubbles:true}));" +
            "}",
            content);

        return this;
    }

    /** Secret is the GitHub default — no action needed. */
    public GistCreatePage selectSecret() {
        return this;
    }

    /** Opens the visibility dropdown and selects Public. */
    public GistCreatePage selectPublic() {
        try {
            click(visibilitySummary);
            click(publicOption);
        } catch (Exception e) {
            // If the dropdown is already showing Public or doesn't exist, ignore
        }
        return this;
    }

    public GistViewPage createGist() {
        click(createGistButton);
        return new GistViewPage();
    }

    public GistViewPage updateGist() {
        click(updateGistButton);
        return new GistViewPage();
    }
}
