package ai;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.AdaptiveWait;
import utils.ScreenshotUtils;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TaskExecutor — the runtime that walks a task plan and produces a result list.
 *
 * <h3>Execution model</h3>
 * <ol>
 *   <li>Each {@link AutonomousTask} in the plan is run in order.</li>
 *   <li>If a task is {@link AutonomousTask#isRetryable() retryable} and fails, it is
 *       retried up to {@link AutonomousTask#maxRetries()} times with an exponential
 *       back-off (500 ms × attempt number) before being recorded as FAIL.</li>
 *   <li>If {@code haltOnFailure} is true (the default), the executor stops after the
 *       first FAIL/ERROR result and marks remaining tasks as SKIP.  Set it to false
 *       for exploratory runs where you want every task attempted regardless.</li>
 *   <li>A screenshot is captured automatically on every FAIL/ERROR and its path is
 *       stored in the {@link TaskResult}.</li>
 *   <li>After all tasks complete, the adaptive-wait timing summary is printed to the log.</li>
 * </ol>
 *
 * Author: Group 5 — Autonomous AI Engine
 */
public class TaskExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(TaskExecutor.class);

    private final WebDriver driver;
    private final boolean   haltOnFailure;

    /**
     * Creates an executor that halts the plan on first failure.
     *
     * @param driver the live WebDriver instance (must not be null)
     */
    public TaskExecutor(WebDriver driver) {
        this(driver, true);
    }

    /**
     * Creates an executor with configurable halt behaviour.
     *
     * @param driver          the live WebDriver instance
     * @param haltOnFailure   when true, remaining tasks are skipped after first FAIL/ERROR
     */
    public TaskExecutor(WebDriver driver, boolean haltOnFailure) {
        if (driver == null) throw new IllegalArgumentException("driver must not be null");
        this.driver        = driver;
        this.haltOnFailure = haltOnFailure;
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Public API
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Executes every task in {@code plan} and returns an ordered, unmodifiable
     * result list of the same length as the plan.
     *
     * @param plan the task list produced by {@link TaskPlanner#plan(String)}
     * @return list of results — one per task, in execution order
     */
    public List<TaskResult> execute(List<AutonomousTask> plan) {
        List<TaskResult> results = new ArrayList<>();
        boolean halted = false;

        LOG.info("\n╔══════════════════════════════════════════════════════════════╗");
        LOG.info("  🤖 AUTONOMOUS EXECUTOR — running {} task(s)", plan.size());
        LOG.info("╚══════════════════════════════════════════════════════════════╝");

        for (AutonomousTask task : plan) {
            if (halted) {
                LOG.info("  ⏭️  SKIP (halted) — {}", task.getName());
                results.add(TaskResult.builder(task.getName())
                        .status(TaskResult.Status.SKIP)
                        .detail("Skipped because a previous task failed")
                        .build());
                continue;
            }

            TaskResult result = executeWithRetry(task);
            results.add(result);

            logResult(result);

            if (result.isFailed() && haltOnFailure) {
                LOG.warn("  🛑  Halting plan after failure in task «{}»", task.getName());
                halted = true;
            }
        }

        // Print adaptive-wait timing analysis at end of run
        AdaptiveWait.logTimingSummary();

        LOG.info("\n══════════════════════ EXECUTION COMPLETE ══════════════════════");
        long passed  = results.stream().filter(TaskResult::isPassed).count();
        long failed  = results.stream().filter(TaskResult::isFailed).count();
        long skipped = results.stream().filter(r -> r.getStatus() == TaskResult.Status.SKIP).count();
        LOG.info("  Results: {} passed  {} failed  {} skipped  (total {})",
                 passed, failed, skipped, results.size());
        LOG.info("════════════════════════════════════════════════════════════════\n");

        return Collections.unmodifiableList(results);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Internal helpers
    // ────────────────────────────────────────────────────────────────────────

    private TaskResult executeWithRetry(AutonomousTask task) {
        int maxAttempts = task.isRetryable() ? task.maxRetries() + 1 : 1;
        TaskResult lastResult = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            if (attempt > 1) {
                long backoffMs = 500L * attempt;
                LOG.info("  🔁  Retry {}/{} for «{}» (backoff {}ms)",
                         attempt - 1, task.maxRetries(), task.getName(), backoffMs);
                try { Thread.sleep(backoffMs); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            Instant start = Instant.now();
            try {
                lastResult = task.execute(driver);
            } catch (Exception uncaught) {
                // Tasks should not throw, but if they do, wrap into an ERROR result
                lastResult = TaskResult.builder(task.getName())
                        .startedAt(start)
                        .finishedAt(Instant.now())
                        .error(uncaught)
                        .build();
            }

            if (!lastResult.isFailed()) break; // success or skip — stop retrying

            // Capture screenshot on failure/error
            if (lastResult.getScreenshotPath() == null) {
                lastResult = attachScreenshot(lastResult, task.getName() + "_attempt" + attempt);
            }
        }

        return lastResult;
    }

    /**
     * Takes a screenshot and returns a rebuilt result with the screenshot path attached.
     */
    private TaskResult attachScreenshot(TaskResult original, String name) {
        try {
            Path shot = ScreenshotUtils.capture("ai_fail_" + name.replaceAll("[^a-zA-Z0-9_-]", "_"));
            if (shot != null) {
                return TaskResult.builder(original.getTaskName())
                        .status(original.getStatus())
                        .startedAt(original.getStartedAt())
                        .finishedAt(original.getFinishedAt())
                        .details(original.getDetails())
                        .screenshotPath(shot.toString())
                        .cause(original.getCause())
                        .build();
            }
        } catch (Exception ignored) { /* screenshot failure must not mask the original failure */ }
        return original;
    }

    private void logResult(TaskResult r) {
        String icon = switch (r.getStatus()) {
            case PASS  -> "✅";
            case FAIL  -> "❌";
            case ERROR -> "💥";
            case SKIP  -> "⏭️";
        };
        LOG.info("  {} [{}] {} ({}ms)", icon, r.getStatus(), r.getTaskName(),
                 r.getDuration().toMillis());
        if (r.isFailed() && r.getCause() != null) {
            LOG.error("     Cause: {}", r.getCause().getMessage());
        }
        if (r.getScreenshotPath() != null) {
            LOG.info("     📸 Screenshot: {}", r.getScreenshotPath());
        }
    }
}
