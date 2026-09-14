package ai;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PageIntelligence — analyses a live page and extracts structured information
 * about every interactive element: buttons, links, form inputs, and nav items.
 *
 * <p>This is the "eyes" of the autonomous engine.  Instead of relying on
 * hard-coded locators, {@code PageIntelligence} scans the DOM at runtime and
 * produces a {@link PageSnapshot} that the {@link ExplorerTask} and other
 * autonomous tasks can reason about.
 *
 * <h3>What it extracts</h3>
 * <ul>
 *   <li><b>Links</b> — every {@code <a href>} visible in the viewport.
 *       Classified as internal / external / same-page / anchor.</li>
 *   <li><b>Buttons</b> — every {@code <button>} and {@code input[type=submit/button/reset]}
 *       that is visible and not disabled.</li>
 *   <li><b>Forms</b> — every {@code <form>} element, with its action URL.</li>
 *   <li><b>Nav items</b> — elements with {@code role="navigation"} or inside
 *       {@code <nav>} tags.</li>
 *   <li><b>Page title / URL</b> — captured at snapshot time.</li>
 * </ul>
 *
 * Author: Group 5 — Autonomous AI Engine
 */
public class PageIntelligence {

    private static final Logger LOG = LoggerFactory.getLogger(PageIntelligence.class);

    private final WebDriver driver;
    private final String    ownHost; // e.g. "github.com"

    public PageIntelligence(WebDriver driver, String ownHost) {
        this.driver  = driver;
        this.ownHost = ownHost;
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Public API
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Scans the current page and returns a {@link PageSnapshot}.
     * This method does NOT navigate — the driver must already be on the target page.
     */
    public PageSnapshot snapshot() {
        String url   = driver.getCurrentUrl();
        String title = driver.getTitle();
        LOG.debug("  [PageIntel] Snapshotting: {} — {}", title, url);

        List<LinkInfo>   links   = extractLinks();
        List<ButtonInfo> buttons = extractButtons();
        List<String>     forms   = extractFormActions();

        LOG.debug("  [PageIntel] Found {} links, {} buttons, {} forms", links.size(), buttons.size(), forms.size());
        return new PageSnapshot(url, title, links, buttons, forms);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Extraction methods
    // ────────────────────────────────────────────────────────────────────────

    private List<LinkInfo> extractLinks() {
        List<LinkInfo> result = new ArrayList<>();
        try {
            List<WebElement> anchors = driver.findElements(By.tagName("a"));
            for (WebElement a : anchors) {
                try {
                    String href = a.getAttribute("href");
                    String text = a.getText().trim();
                    if (href == null || href.isBlank() || href.startsWith("javascript:")) continue;
                    if (!a.isDisplayed()) continue;

                    LinkType type = classifyLink(href);
                    result.add(new LinkInfo(text.isEmpty() ? href : text, href, type));
                } catch (Exception ignored) { /* stale element — skip */ }
            }
        } catch (Exception e) {
            LOG.warn("  [PageIntel] Link extraction failed: {}", e.getMessage());
        }
        return result;
    }

    private List<ButtonInfo> extractButtons() {
        List<ButtonInfo> result = new ArrayList<>();
        try {
            List<WebElement> elements = driver.findElements(
                    By.cssSelector("button:not([disabled]), input[type='submit']:not([disabled]), " +
                                   "input[type='button']:not([disabled]), [role='button']"));
            for (WebElement el : elements) {
                try {
                    if (!el.isDisplayed()) continue;
                    String label = el.getText().trim();
                    if (label.isEmpty()) label = el.getAttribute("aria-label");
                    if (label == null || label.isBlank()) label = el.getAttribute("value");
                    if (label == null || label.isBlank()) label = el.getAttribute("title");
                    if (label == null) label = "(unlabelled)";
                    result.add(new ButtonInfo(label.trim(), el.getTagName(),
                                              el.getAttribute("type"), el.getAttribute("class")));
                } catch (Exception ignored) { /* stale — skip */ }
            }
        } catch (Exception e) {
            LOG.warn("  [PageIntel] Button extraction failed: {}", e.getMessage());
        }
        return result;
    }

    private List<String> extractFormActions() {
        List<String> result = new ArrayList<>();
        try {
            List<WebElement> forms = driver.findElements(By.tagName("form"));
            for (WebElement form : forms) {
                try {
                    String action = form.getAttribute("action");
                    result.add(action != null ? action : "(no action)");
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            LOG.warn("  [PageIntel] Form extraction failed: {}", e.getMessage());
        }
        return result;
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Link classification
    // ────────────────────────────────────────────────────────────────────────

    private LinkType classifyLink(String href) {
        if (href.startsWith("#"))                        return LinkType.ANCHOR;
        if (!href.startsWith("http"))                   return LinkType.INTERNAL;
        if (href.contains(ownHost))                     return LinkType.INTERNAL;
        return LinkType.EXTERNAL;
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Nested data classes
    // ────────────────────────────────────────────────────────────────────────

    public enum LinkType { INTERNAL, EXTERNAL, ANCHOR }

    public record LinkInfo(String text, String href, LinkType type) {}

    public record ButtonInfo(String label, String tag, String type, String cssClass) {}

    /** Immutable snapshot of a page's interactive elements at a point in time. */
    public static final class PageSnapshot {
        private final String           url;
        private final String           title;
        private final List<LinkInfo>   links;
        private final List<ButtonInfo> buttons;
        private final List<String>     formActions;

        PageSnapshot(String url, String title,
                     List<LinkInfo> links, List<ButtonInfo> buttons, List<String> formActions) {
            this.url         = url;
            this.title       = title;
            this.links       = Collections.unmodifiableList(links);
            this.buttons     = Collections.unmodifiableList(buttons);
            this.formActions = Collections.unmodifiableList(formActions);
        }

        public String           getUrl()         { return url; }
        public String           getTitle()        { return title; }
        public List<LinkInfo>   getLinks()        { return links; }
        public List<ButtonInfo> getButtons()      { return buttons; }
        public List<String>     getFormActions()  { return formActions; }

        public List<LinkInfo> getInternalLinks() {
            return links.stream().filter(l -> l.type() == LinkType.INTERNAL).toList();
        }

        public List<LinkInfo> getExternalLinks() {
            return links.stream().filter(l -> l.type() == LinkType.EXTERNAL).toList();
        }

        @Override
        public String toString() {
            return String.format("PageSnapshot[url=%s, links=%d, buttons=%d, forms=%d]",
                    url, links.size(), buttons.size(), formActions.size());
        }
    }
}
