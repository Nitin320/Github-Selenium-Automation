package utils;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AdaptiveWait — replaces fixed explicit/implicit waits with a self-calibrating
 * timing engine that learns from real element response times during the test run.
 *
 * <h3>The problem with fixed waits</h3>
 * <ul>
 *   <li>{@code Thread.sleep(2000)} always wastes time on fast machines.</li>
 *   <li>A fixed 15-second {@link WebDriverWait} polls every 500 ms for the
 *       full duration when the element is genuinely absent.</li>
 *   <li>Implicit waits stack with explicit waits and make
 *       {@code NoSuchElementException} timing unpredictable.</li>
 * </ul>
 *
 * <h3>How adaptive timing works</h3>
 * <ol>
 *   <li>Every time an element is successfully located, the actual wall-clock
 *       wait time is recorded in an in-memory histogram keyed by locator string.</li>
 *   <li>The next time the same locator is requested, the timeout is calculated as:
 *       {@code max(MIN_WAIT, observedAverage × SAFETY_FACTOR + BUFFER_MS)} —
 *       giving a tight-but-safe window based on historical performance.</li>
 *   <li>When a locator is seen for the first time, the configured
 *       {@code DEFAULT_TIMEOUT_SEC} from config.properties is used.</li>
 *   <li>On timeout, the observation is recorded as a "slow" sample at the
 *       full timeout value so future attempts start with a wider window.</li>
 * </ol>
 *
 * <h3>Result</h3>
 * Fast pages converge toward 1–3 second wait windows after just a few runs.
 * Slow pages (e.g. repo creation confirmation) keep their wider window without
 * requiring any manual configuration change.
 *
 * Author: Group 5 — Adaptive timing
 */
public class AdaptiveWait {

    private static final Logger LOG = LoggerFactory.getLogger(AdaptiveWait.class);

    // ── Timing constants ─────────────────────────────────────────────────────

    /** Minimum timeout floor — never wait less than this, even for fast elements. */
    private static final long MIN_WAIT_MS = 1_000;

    /** Safety multiplier applied to the historical average. */
    private static final double SAFETY_FACTOR = 1.5;

    /** Fixed buffer added on top of the multiplied average (milliseconds). */
    private static final long BUFFER_MS = 500;

    /** How often WebDriverWait polls the DOM (milliseconds). */
    private static final long POLL_INTERVAL_MS = 200;

    /** Maximum number of samples kept per locator to avoid unbounded memory growth. */
    private static final int MAX_SAMPLES = 50;

    // ── Per-locator timing history ────────────────────────────────────────────

    /**
     * Stores a running sum and count of observed wait times (in ms) for each
     * locator string.  Key = {@code locator.toString()}, Value = long[]{sum, count}.
     */
    private static final Map<String, long[]> TIMING_HISTORY = new ConcurrentHashMap<>();

    private AdaptiveWait() {}

    // ────────────────────────────────────────────────────────────────────────
    //  Public API
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Waits for the element identified by {@code locator} to become visible,
     * using an adaptive timeout computed from historical observation.
     *
     * @param driver  the active WebDriver
     * @param locator the By locator to wait for
     * @return the visible {@link WebElement}
     * @throws TimeoutException if the element is not visible within the adaptive timeout
     */
    public static WebElement waitForVisibility(WebDriver driver, By locator) {
        long timeoutMs = computeTimeout(locator);
        long start = System.currentTimeMillis();
        try {
            WebElement el = buildWait(driver, timeoutMs)
                    .until(d -> {
                        try {
                            WebElement e = d.findElement(locator);
                            return (e != null && e.isDisplayed()) ? e : null;
                        } catch (Exception ignored) {
                            return null;
                        }
                    });
            recordSuccess(locator, System.currentTimeMillis() - start);
            return el;
        } catch (TimeoutException te) {
            recordTimeout(locator, timeoutMs);
            throw te;
        }
    }

    /**
     * Waits for the element identified by {@code locator} to become clickable,
     * using an adaptive timeout.
     *
     * @param driver  the active WebDriver
     * @param locator the By locator to wait for
     * @return the clickable {@link WebElement}
     */
    public static WebElement waitForClickable(WebDriver driver, By locator) {
        long timeoutMs = computeTimeout(locator);
        long start = System.currentTimeMillis();
        try {
            WebElement el = buildWait(driver, timeoutMs)
                    .until(d -> {
                        try {
                            WebElement e = d.findElement(locator);
                            return (e != null && e.isDisplayed() && e.isEnabled()) ? e : null;
                        } catch (Exception ignored) {
                            return null;
                        }
                    });
            recordSuccess(locator, System.currentTimeMillis() - start);
            return el;
        } catch (TimeoutException te) {
            recordTimeout(locator, timeoutMs);
            throw te;
        }
    }

    /**
     * Waits for a custom {@link ExpectedCondition} with an adaptive timeout
     * derived from the provided label (used as the histogram key).
     *
     * @param driver    the active WebDriver
     * @param condition the condition to wait for
     * @param label     a descriptive key used for history tracking (e.g. "loginSuccess")
     * @param <T>       the return type of the condition
     * @return the condition result
     */
    public static <T> T waitFor(WebDriver driver, ExpectedCondition<T> condition, String label) {
        By syntheticKey = By.id("__adaptive__" + label);
        long timeoutMs  = computeTimeout(syntheticKey);
        long start      = System.currentTimeMillis();
        try {
            T result = buildWait(driver, timeoutMs).until(condition);
            recordSuccess(syntheticKey, System.currentTimeMillis() - start);
            return result;
        } catch (TimeoutException te) {
            recordTimeout(syntheticKey, timeoutMs);
            throw te;
        }
    }

    /**
     * Checks whether an element is present without throwing — returns quickly
     * using only the minimum wait floor.
     *
     * @param driver  the active WebDriver
     * @param locator the By locator to probe
     * @return {@code true} if the element is visible within the minimum wait
     */
    public static boolean isPresent(WebDriver driver, By locator) {
        try {
            buildWait(driver, MIN_WAIT_MS).until(d -> {
                try {
                    WebElement e = d.findElement(locator);
                    return (e != null && e.isDisplayed()) ? e : null;
                } catch (Exception ignored) {
                    return null;
                }
            });
            return true;
        } catch (TimeoutException ignored) {
            return false;
        }
    }

    /**
     * Prints a summary of all accumulated timing observations to the log.
     * Useful at the end of a test run to identify which locators are slowest.
     */
    public static void logTimingSummary() {
        if (TIMING_HISTORY.isEmpty()) {
            LOG.info("  [AdaptiveWait] No timing observations recorded yet.");
            return;
        }
        LOG.info("  ┌─── AdaptiveWait Timing Summary ──────────────────────────────────");
        TIMING_HISTORY.entrySet().stream()
                .sorted((a, b) -> Long.compare(avg(b.getValue()), avg(a.getValue())))
                .forEach(e -> {
                    long[] v = e.getValue();
                    String key = e.getKey().length() > 60
                            ? "…" + e.getKey().substring(e.getKey().length() - 59)
                            : e.getKey();
                    LOG.info("  │  avg={} ms  samples={}  locator={}",
                             String.format("%5d", avg(v)),
                             String.format("%3d", v[1]),
                             key);
                });
        LOG.info("  └──────────────────────────────────────────────────────────────────");
    }

    /**
     * Returns the current adaptive timeout (in ms) that would be used for the
     * given locator.  Useful for assertions in tests about timing behaviour.
     */
    public static long currentTimeoutMs(By locator) {
        return computeTimeout(locator);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Internal helpers
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Computes the adaptive timeout for {@code locator}.
     *
     * <p>Formula: {@code max(MIN_WAIT_MS, average × SAFETY_FACTOR + BUFFER_MS)}
     * <br>First-time locators use the configured {@code explicit.wait} seconds.
     */
    private static long computeTimeout(By locator) {
        String key = locator.toString();
        long[] hist = TIMING_HISTORY.get(key);
        if (hist == null || hist[1] == 0) {
            // No history — use the configured default
            long defaultMs = Long.parseLong(
                    ConfigReader.getProperty("explicit.wait", "15")) * 1_000L;
            LOG.debug("  [AdaptiveWait] No history for '{}' — using default {}ms", key, defaultMs);
            return defaultMs;
        }
        long adaptiveMs = Math.max(MIN_WAIT_MS,
                (long) (avg(hist) * SAFETY_FACTOR) + BUFFER_MS);
        LOG.debug("  [AdaptiveWait] '{}' → avg={}ms, adaptive timeout={}ms",
                  key, avg(hist), adaptiveMs);
        return adaptiveMs;
    }

    private static void recordSuccess(By locator, long elapsedMs) {
        String key = locator.toString();
        TIMING_HISTORY.compute(key, (k, v) -> {
            if (v == null) v = new long[]{0L, 0L};
            // Cap sample count to avoid unbounded accumulation over a very long run
            if (v[1] >= MAX_SAMPLES) {
                // Slide the window: drop oldest by recalculating sum without it
                long oldAvg = avg(v);
                v[0] = (oldAvg * (MAX_SAMPLES - 1)) + elapsedMs;
                v[1] = MAX_SAMPLES;
            } else {
                v[0] += elapsedMs;
                v[1]++;
            }
            return v;
        });
    }

    private static void recordTimeout(By locator, long timeoutMs) {
        // Count a timeout as a "slow" observation at the full timeout value
        // so the next attempt starts with at least as wide a window.
        recordSuccess(locator, timeoutMs);
    }

    private static WebDriverWait buildWait(WebDriver driver, long timeoutMs) {
        return new WebDriverWait(
                driver,
                Duration.ofMillis(timeoutMs),
                Duration.ofMillis(POLL_INTERVAL_MS));
    }

    private static long avg(long[] hist) {
        return hist[1] == 0 ? 0 : hist[0] / hist[1];
    }
}
