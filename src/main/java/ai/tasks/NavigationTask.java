package ai.tasks;

import ai.AutonomousTask;
import ai.TaskResult;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.AdaptiveWait;
import utils.ConfigReader;

import java.time.Instant;

/**
 * NavigationTask — navigates the browser to a specific URL and confirms arrival.
 *
 * <p>Arrival is confirmed by checking that the current URL contains the expected
 * path fragment (everything after the host in the target URL).  Uses
 * {@link AdaptiveWait} for the URL-change confirmation so the wait window
 * tightens over repeated runs.
 *
 * Author: Group 5 — Autonomous AI Engine
 */
public class NavigationTask implements AutonomousTask {

    private static final Logger LOG = LoggerFactory.getLogger(NavigationTask.class);

    private final String targetUrl;

    public NavigationTask(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    @Override
    public String getName() { return "Navigate:" + targetUrl; }

    @Override
    public boolean isRetryable() { return true; }

    @Override
    public TaskResult execute(WebDriver driver) {
        Instant start = Instant.now();
        TaskResult.Builder b = TaskResult.builder(getName()).startedAt(start);

        try {
            LOG.info("  [NavTask] Navigating to: {}", targetUrl);
            driver.get(targetUrl);

            // Adaptive wait for the page to finish loading
            AdaptiveWait.waitFor(driver,
                    d -> "complete".equals(
                            ((org.openqa.selenium.JavascriptExecutor) d)
                                    .executeScript("return document.readyState")),
                    "pageLoad:" + targetUrl);

            String currentUrl = driver.getCurrentUrl();
            LOG.info("  [NavTask] Landed on: {}", currentUrl);

            // Confirm: extract path from targetUrl and check it appears in currentUrl
            String expectedFragment = extractPath(targetUrl);
            if (currentUrl.contains(expectedFragment) || expectedFragment.isBlank()) {
                return b.finishedAt(Instant.now())
                        .status(TaskResult.Status.PASS)
                        .detail("Navigated to: " + currentUrl)
                        .build();
            } else {
                return b.finishedAt(Instant.now())
                        .fail("Expected URL to contain «" + expectedFragment + "» but got: " + currentUrl)
                        .build();
            }
        } catch (Exception e) {
            LOG.error("  [NavTask] ❌ Exception: {}", e.getMessage());
            return b.finishedAt(Instant.now()).error(e).build();
        }
    }

    private String extractPath(String url) {
        try {
            java.net.URI uri = java.net.URI.create(url);
            String path = uri.getPath();
            return (path != null && !path.equals("/")) ? path : "";
        } catch (Exception e) {
            return "";
        }
    }
}
