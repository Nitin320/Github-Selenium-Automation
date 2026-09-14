package ai.tasks;

import ai.AutonomousTask;
import ai.TaskResult;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import session.SessionManager;

import java.time.Instant;

/**
 * LoginTask — autonomous task that ensures the driver has an active GitHub session.
 *
 * <p>Delegates entirely to {@link SessionManager#ensureLoggedIn()} which either
 * restores cookies (fast, ~1 s) or performs a real login (slow, ~10–20 s) depending
 * on whether a cached session exists from a previous task in the same run.
 *
 * Author: Group 5 — Autonomous AI Engine
 */
public class LoginTask implements AutonomousTask {

    private static final Logger LOG = LoggerFactory.getLogger(LoginTask.class);

    @Override
    public String getName() { return "Login"; }

    @Override
    public boolean isRetryable() { return true; }

    @Override
    public int maxRetries() { return 1; }

    @Override
    public TaskResult execute(WebDriver driver) {
        Instant start = Instant.now();
        TaskResult.Builder b = TaskResult.builder(getName()).startedAt(start);

        try {
            LOG.info("  [LoginTask] Ensuring GitHub session is active…");
            boolean ok = SessionManager.ensureLoggedIn();

            if (ok) {
                LOG.info("  [LoginTask] ✅ Session active");
                return b.finishedAt(Instant.now())
                        .status(TaskResult.Status.PASS)
                        .detail("GitHub session established successfully")
                        .build();
            } else {
                return b.finishedAt(Instant.now())
                        .fail("SessionManager.ensureLoggedIn() returned false — " +
                              "check GITHUB_USERNAME and GITHUB_PASSWORD environment variables")
                        .build();
            }
        } catch (Exception e) {
            LOG.error("  [LoginTask] ❌ Exception: {}", e.getMessage());
            return b.finishedAt(Instant.now()).error(e).build();
        }
    }
}
