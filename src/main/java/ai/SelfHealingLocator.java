package ai;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.AdaptiveWait;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SelfHealingLocator — a smart locator wrapper that automatically tries
 * alternative strategies when the primary locator fails to find an element.
 *
 * <h3>Problem</h3>
 * GitHub frequently ships React redesigns that change CSS classes, data-testid
 * attributes, and element hierarchies — breaking fixed locators silently.
 *
 * <h3>Healing strategies (tried in order)</h3>
 * <ol>
 *   <li><b>Primary locator</b> — the original By specified by the page object.</li>
 *   <li><b>Text-based XPath</b> — searches for any element whose visible text
 *       matches the supplied {@code label} (normalised, case-insensitive).</li>
 *   <li><b>ARIA label search</b> — looks for {@code aria-label} attributes
 *       containing the label text.</li>
 *   <li><b>Partial CSS fallback chain</b> — tries each entry in the
 *       {@code fallbacks} list supplied at construction time.</li>
 *   <li><b>JavaScript heuristic</b> — as a last resort, asks the browser to
 *       find any visible, enabled element whose text or attribute contains the label.</li>
 * </ol>
 *
 * <h3>Learning</h3>
 * When a non-primary strategy succeeds, the winning strategy is remembered in a
 * JVM-wide cache keyed by the primary locator string.  Subsequent calls skip
 * failed strategies and start from the last known-good one, making the locator
 * progressively faster over a long test run.
 *
 * Author: Group 5 — Autonomous AI Engine
 */
public class SelfHealingLocator {

    private static final Logger LOG = LoggerFactory.getLogger(SelfHealingLocator.class);

    /** JVM-wide cache: primary locator string → index of last successful strategy. */
    private static final Map<String, Integer> WINNING_STRATEGY = new ConcurrentHashMap<>();

    // ── Instance state ────────────────────────────────────────────────────────

    private final By     primary;
    private final String label;
    private final List<By> fallbacks;

    // ────────────────────────────────────────────────────────────────────────
    //  Constructors
    // ────────────────────────────────────────────────────────────────────────

    /**
     * @param primary   the primary By locator
     * @param label     human-readable text used for text-based and ARIA healing
     * @param fallbacks additional By locators tried in order after primary fails
     */
    public SelfHealingLocator(By primary, String label, By... fallbacks) {
        this.primary   = primary;
        this.label     = label;
        this.fallbacks = Arrays.asList(fallbacks);
    }

    /** Convenience constructor — no additional fallbacks. */
    public SelfHealingLocator(By primary, String label) {
        this(primary, label, new By[0]);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Public API
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Finds and returns the element using the best available strategy.
     *
     * @param driver the active WebDriver
     * @return the located {@link WebElement}
     * @throws NoSuchElementException if all strategies are exhausted
     */
    public WebElement find(WebDriver driver) {
        String cacheKey = primary.toString();
        int startStrategy = WINNING_STRATEGY.getOrDefault(cacheKey, 0);

        // Build the full ordered strategy list for this call
        List<By> strategies = buildStrategies();

        // Try from the last known-good strategy first, then wrap around
        for (int offset = 0; offset < strategies.size(); offset++) {
            int index = (startStrategy + offset) % strategies.size();
            By strategy = strategies.get(index);
            try {
                WebElement el = AdaptiveWait.waitForVisibility(driver, strategy);
                if (el != null) {
                    if (index != 0) {
                        LOG.info("  [SelfHealing] «{}» healed using strategy #{}: {}",
                                 label, index, strategy);
                        WINNING_STRATEGY.put(cacheKey, index);
                    }
                    return el;
                }
            } catch (Exception ignored) {
                // This strategy failed — try the next one
            }
        }

        // All explicit strategies failed — try the JS heuristic
        WebElement jsResult = tryJsHeuristic(driver);
        if (jsResult != null) {
            LOG.info("  [SelfHealing] «{}» located via JS heuristic", label);
            return jsResult;
        }

        throw new NoSuchElementException(
            "SelfHealingLocator could not find element «" + label + "» using any strategy");
    }

    /**
     * Returns {@code true} if the element can be found using any strategy within
     * the adaptive minimum wait window.
     */
    public boolean isPresent(WebDriver driver) {
        try {
            find(driver);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Internal helpers
    // ────────────────────────────────────────────────────────────────────────

    private List<By> buildStrategies() {
        java.util.List<By> list = new java.util.ArrayList<>();
        list.add(primary);                                             // 0: primary
        list.add(By.xpath("//*[normalize-space(text())='" + label + "']"));  // 1: exact text
        list.add(By.xpath("//*[contains(normalize-space(.), '" + label + "')]")); // 2: contains text
        list.add(By.cssSelector("[aria-label*='" + label + "']"));    // 3: aria-label
        list.add(By.cssSelector("[title*='" + label + "']"));          // 4: title attr
        list.addAll(fallbacks);                                        // 5+: explicit fallbacks
        return list;
    }

    @SuppressWarnings("unchecked")
    private WebElement tryJsHeuristic(WebDriver driver) {
        try {
            String script =
                "var label = arguments[0].toLowerCase();" +
                "var all = document.querySelectorAll('button,a,input,select,textarea,[role=\"button\"],[role=\"link\"]');" +
                "for (var i = 0; i < all.length; i++) {" +
                "  var el = all[i];" +
                "  if (el.offsetParent === null) continue;" +  // skip hidden
                "  var text = (el.textContent || el.value || el.placeholder || '').toLowerCase().trim();" +
                "  var aria  = (el.getAttribute('aria-label') || '').toLowerCase();" +
                "  var title = (el.getAttribute('title') || '').toLowerCase();" +
                "  if (text.includes(label) || aria.includes(label) || title.includes(label)) {" +
                "    return el;" +
                "  }" +
                "}" +
                "return null;";
            return (WebElement) ((JavascriptExecutor) driver).executeScript(script, label.toLowerCase());
        } catch (Exception e) {
            return null;
        }
    }
}
