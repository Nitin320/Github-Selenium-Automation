package ai.tasks;

import ai.AutonomousTask;
import ai.TaskResult;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.AdaptiveWait;

import java.time.Instant;

/**
 * AssertionTask — evaluates a declarative assertion expression against the live page.
 *
 * <h3>Supported expression syntax</h3>
 * <pre>
 *   "url contains /settings"     → driver.getCurrentUrl().contains("/settings")
 *   "url equals https://…"       → driver.getCurrentUrl().equals("…")
 *   "title contains Dashboard"   → driver.getTitle().contains("Dashboard")
 *   "element visible #my-id"     → element identified by CSS selector is visible
 *   "element present .my-class"  → element exists in DOM (not necessarily visible)
 *   "text contains Welcome"      → page source contains "Welcome"
 * </pre>
 *
 * Expressions are case-insensitive for the keyword parts; the value after the
 * final keyword is compared as-is (case-sensitive) since URLs and CSS selectors
 * are case-sensitive on GitHub.
 *
 * Author: Group 5 — Autonomous AI Engine
 */
public class AssertionTask implements AutonomousTask {

    private static final Logger LOG = LoggerFactory.getLogger(AssertionTask.class);

    private final String expression;

    public AssertionTask(String expression) {
        this.expression = expression;
    }

    @Override
    public String getName() { return "Assert:" + expression; }

    @Override
    public TaskResult execute(WebDriver driver) {
        Instant start = Instant.now();
        TaskResult.Builder b = TaskResult.builder(getName()).startedAt(start);

        try {
            String lower = expression.toLowerCase();

            // ── URL assertions ───────────────────────────────────────────
            if (lower.startsWith("url contains ")) {
                String expected = expression.substring("url contains ".length()).trim();
                String actual   = driver.getCurrentUrl();
                boolean ok = actual.contains(expected);
                LOG.info("  [Assert] url contains «{}» → {}", expected, ok);
                return ok
                    ? b.finishedAt(Instant.now()).status(TaskResult.Status.PASS)
                        .detail("PASS: URL «" + actual + "» contains «" + expected + "»").build()
                    : b.finishedAt(Instant.now())
                        .fail("URL «" + actual + "» does not contain «" + expected + "»").build();
            }

            if (lower.startsWith("url equals ")) {
                String expected = expression.substring("url equals ".length()).trim();
                String actual   = driver.getCurrentUrl();
                boolean ok = actual.equals(expected);
                LOG.info("  [Assert] url equals «{}» → {}", expected, ok);
                return ok
                    ? b.finishedAt(Instant.now()).status(TaskResult.Status.PASS)
                        .detail("PASS: URL equals «" + expected + "»").build()
                    : b.finishedAt(Instant.now())
                        .fail("Expected URL «" + expected + "» but got «" + actual + "»").build();
            }

            // ── Title assertions ─────────────────────────────────────────
            if (lower.startsWith("title contains ")) {
                String expected = expression.substring("title contains ".length()).trim();
                String actual   = driver.getTitle();
                boolean ok = actual.contains(expected);
                LOG.info("  [Assert] title contains «{}» → {}", expected, ok);
                return ok
                    ? b.finishedAt(Instant.now()).status(TaskResult.Status.PASS)
                        .detail("PASS: title «" + actual + "» contains «" + expected + "»").build()
                    : b.finishedAt(Instant.now())
                        .fail("Title «" + actual + "» does not contain «" + expected + "»").build();
            }

            // ── Element visibility ───────────────────────────────────────
            if (lower.startsWith("element visible ")) {
                String selector = expression.substring("element visible ".length()).trim();
                boolean visible = AdaptiveWait.isPresent(driver, By.cssSelector(selector));
                LOG.info("  [Assert] element visible «{}» → {}", selector, visible);
                return visible
                    ? b.finishedAt(Instant.now()).status(TaskResult.Status.PASS)
                        .detail("PASS: element «" + selector + "» is visible").build()
                    : b.finishedAt(Instant.now())
                        .fail("Element «" + selector + "» is not visible").build();
            }

            // ── Element presence ─────────────────────────────────────────
            if (lower.startsWith("element present ")) {
                String selector = expression.substring("element present ".length()).trim();
                boolean present = !driver.findElements(By.cssSelector(selector)).isEmpty();
                LOG.info("  [Assert] element present «{}» → {}", selector, present);
                return present
                    ? b.finishedAt(Instant.now()).status(TaskResult.Status.PASS)
                        .detail("PASS: element «" + selector + "» is in the DOM").build()
                    : b.finishedAt(Instant.now())
                        .fail("Element «" + selector + "» is not present in the DOM").build();
            }

            // ── Page source text ─────────────────────────────────────────
            if (lower.startsWith("text contains ")) {
                String expected = expression.substring("text contains ".length()).trim();
                boolean ok = driver.getPageSource().contains(expected);
                LOG.info("  [Assert] text contains «{}» → {}", expected, ok);
                return ok
                    ? b.finishedAt(Instant.now()).status(TaskResult.Status.PASS)
                        .detail("PASS: page source contains «" + expected + "»").build()
                    : b.finishedAt(Instant.now())
                        .fail("Page source does not contain «" + expected + "»").build();
            }

            // ── Unrecognised ─────────────────────────────────────────────
            LOG.warn("  [Assert] Unrecognised assertion expression: «{}»", expression);
            return b.finishedAt(Instant.now())
                    .status(TaskResult.Status.SKIP)
                    .detail("SKIP: unrecognised assertion expression «" + expression + "»")
                    .build();

        } catch (Exception e) {
            LOG.error("  [Assert] ❌ Exception: {}", e.getMessage());
            return b.finishedAt(Instant.now()).error(e).build();
        }
    }
}
