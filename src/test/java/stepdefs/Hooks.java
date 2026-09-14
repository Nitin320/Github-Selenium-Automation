package stepdefs;

import driver.DriverFactory;
import driver.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reporting.ReportManager;
import session.SessionManager;
import utils.AdaptiveWait;
import utils.ScreenshotUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Hooks — single, centralised Cucumber lifecycle controller.
 *
 * <p>ALL Cucumber step definition classes must NOT have their own @Before/@After.
 * This class owns the full driver lifecycle and reporting for every scenario.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Print a clearly formatted banner to the console for every scenario so
 *       the terminal shows exactly which scenario is running, which feature it
 *       belongs to, and which team member authored it.</li>
 *   <li>Start a fresh WebDriver before every scenario.</li>
 *   <li>Take a screenshot after EVERY step so the report shows a visual trail
 *       of what happened at each point in the scenario.</li>
 *   <li>Take an explicit failure screenshot and log PASS/FAIL to ExtentReports
 *       after the scenario ends.</li>
 *   <li>Quit the driver cleanly so no browser windows are leaked.</li>
 * </ul>
 *
 * Author: Team — Group 5
 */
public class Hooks {

    private static final Logger LOG = LoggerFactory.getLogger(Hooks.class);

    // =========================================================================
    //  @BeforeAll — runs once before the entire test suite
    // =========================================================================

    /**
     * Deletes all files in {@code target/screenshots/} at the start of each run
     * so that screenshots from a previous run do not accumulate alongside new ones.
     */
    @BeforeAll
    public static void cleanScreenshotFolder() {
        Path screenshotsDir = Path.of("target", "screenshots");
        if (Files.exists(screenshotsDir)) {
            try {
                Files.walk(screenshotsDir)
                     .sorted(Comparator.reverseOrder())  // files before their parent directories
                     .filter(Files::isRegularFile)
                     .forEach(f -> {
                         try { Files.delete(f); }
                         catch (IOException ignore) { /* non-fatal */ }
                     });
                LOG.info("  🧹  Cleared target/screenshots/ before run");
            } catch (IOException e) {
                LOG.warn("  ⚠️  Could not clean screenshots directory: {}", e.getMessage());
            }
        }
    }

    // ── Feature → author mapping ──────────────────────────────────────────
    // Keeps the console output informative without requiring annotation changes
    // on every single step class.
    private static final java.util.Map<String, String> FEATURE_AUTHOR = new java.util.LinkedHashMap<>();
    static {
        FEATURE_AUTHOR.put("Login",          "Jothi Sri");
        FEATURE_AUTHOR.put("Logout",         "Jothi Sri");
        FEATURE_AUTHOR.put("Gist",           "Naveen");
        FEATURE_AUTHOR.put("Issues",         "Deva Vignan");
        FEATURE_AUTHOR.put("Repository",     "Sujin");
        FEATURE_AUTHOR.put("Search",         "Yazeen");
        FEATURE_AUTHOR.put("Code Viewer",    "Neil Joe Augustine");
        FEATURE_AUTHOR.put("Profile",        "Jothi Sri");
    }

    // ── Step counter (used for naming per-step screenshots) ───────────────
    private int stepCounter;

    // ── Current scenario name (used for screenshot filenames) ─────────────
    private String safeScenarioName;

    // =========================================================================
    //  @Before — runs once before EVERY scenario
    // =========================================================================

    @Before(order = 0)
    public void startScenario(Scenario scenario) {
        stepCounter = 0;
        safeScenarioName = scenario.getName().replaceAll("[^a-zA-Z0-9_-]", "_");

        // Derive the feature name from the first URI segment after "features/"
        String uri         = scenario.getUri().toString();
        String featureFile = uri.contains("/") ? uri.substring(uri.lastIndexOf('/') + 1) : uri;
        featureFile        = featureFile.replace(".feature", "");
        String featureName = capitalise(featureFile.replace("_", " ").replace("-", " "));
        String author      = resolveAuthor(featureName);

        String banner = "\n"
            + "╔══════════════════════════════════════════════════════════════╗\n"
            + "  🚀 SCENARIO STARTING\n"
            + "  Feature  : " + featureName + "\n"
            + "  Scenario : " + scenario.getName() + "\n"
            + "  Author   : " + author + "\n"
            + "  Tags     : " + scenario.getSourceTagNames() + "\n"
            + "╚══════════════════════════════════════════════════════════════╝";
        LOG.info(banner);

        // Start a fresh driver for this scenario.
        // NOTE: We do NOT call SessionManager.ensureLoggedIn() here — that is the
        // responsibility of the step definitions that require a session
        // (the "the user is logged into GitHub" Given step in GistSteps / IssueSteps,
        // and "I am logged in to GitHub" in CodeViewerSteps).  SessionManager caches
        // the session cookies after the first real login so that every subsequent
        // scenario reuses them via a fast cookie-inject + refresh (~1s) instead of
        // a full login form interaction (~10–20s).
        WebDriver driver = DriverFactory.createDriver();
        DriverManager.setDriver(driver);

        // Start an ExtentReports node for this scenario
        ReportManager.startTest(
            scenario.getName(),
            "Feature: " + featureName + "  |  Author: " + author
        );
    }

    // =========================================================================
    //  @AfterStep — runs after EVERY step, captures a screenshot for the report
    // =========================================================================

    @AfterStep
    public void afterEachStep(Scenario scenario) {
        stepCounter++;
        if (DriverManager.getDriver() != null) {
            String shotName = safeScenarioName + "_step" + stepCounter;
            try {
                ScreenshotUtils.capture(shotName);
                LOG.info("  📸 Screenshot saved  →  screenshots/{}.png", shotName);
            } catch (Exception e) {
                LOG.warn("  ⚠️  Screenshot failed at step {}: {}", stepCounter, e.getMessage());
            }
        }
    }

    // =========================================================================
    //  @After — runs once after EVERY scenario
    // =========================================================================

    @After(order = 0)
    public void endScenario(Scenario scenario) {
        // If the scenario explicitly signed out, invalidate the cached session
        // so the next scenario does a fresh login rather than planting stale cookies.
        if (scenario.getSourceTagNames().contains("@logout")
                || scenario.getName().toLowerCase().contains("logout")
                || scenario.getName().toLowerCase().contains("sign out")) {
            SessionManager.invalidateSession();
            LOG.info("  🗑️  Session cache invalidated after logout scenario");
        }

        // Print adaptive-wait timing summary once per scenario at DEBUG level
        if (LOG.isDebugEnabled()) {
            AdaptiveWait.logTimingSummary();
        }

        String status = scenario.isFailed() ? "❌ FAILED" : "✅ PASSED";

        if (scenario.isFailed()) {
            // Dedicated failure screenshot (may contain the error state)
            if (DriverManager.getDriver() != null) {
                String failShot = safeScenarioName + "_FAILED";
                try {
                    ScreenshotUtils.capture(failShot);
                    LOG.error("  📸 Failure screenshot  →  screenshots/{}.png", failShot);
                } catch (Exception e) {
                    LOG.warn("  ⚠️  Could not capture failure screenshot: {}", e.getMessage());
                }
            }
            // Log failure details to ExtentReports
            if (ReportManager.getTest() != null) {
                ReportManager.getTest().fail(
                    "Scenario FAILED — check screenshots/" + safeScenarioName + "_FAILED.png"
                );
            }
            LOG.error(
                "\n╔══════════════════════════════════════════════════════════════╗\n"
                + "  {} : {}\n"
                + "  See screenshots/{}_FAILED.png for the failure state.\n"
                + "╚══════════════════════════════════════════════════════════════╝",
                status, scenario.getName(), safeScenarioName
            );
        } else {
            if (ReportManager.getTest() != null) {
                ReportManager.getTest().pass("Scenario PASSED");
            }
            LOG.info(
                "\n╔══════════════════════════════════════════════════════════════╗\n"
                + "  {} : {}\n"
                + "╚══════════════════════════════════════════════════════════════╝",
                status, scenario.getName()
            );
        }

        // Flush ExtentReports after every scenario so partial results survive a crash
        ReportManager.flushReports();

        // Always quit the driver — but keep session cookies alive in SessionManager
        // so the NEXT scenario can reuse them without another full login.
        try {
            DriverManager.quitDriver();
        } catch (Exception e) {
            LOG.warn("  ⚠️  Driver quit threw an exception: {}", e.getMessage());
        }
    }

    // =========================================================================
    //  Helpers
    // =========================================================================

    private String resolveAuthor(String featureName) {
        for (java.util.Map.Entry<String, String> entry : FEATURE_AUTHOR.entrySet()) {
            if (featureName.toLowerCase().contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }
        return "Team — Group 5";
    }

    private static String capitalise(String s) {
        if (s == null || s.isEmpty()) return s;
        String[] words = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                  .append(w.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }
}
