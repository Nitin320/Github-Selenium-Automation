package ai;

import org.openqa.selenium.WebDriver;

/**
 * AutonomousTask — the single interface every task in the autonomous engine implements.
 *
 * <p>A task is a self-contained unit of work: navigate somewhere, click something,
 * assert something, explore a page, etc.  Tasks compose into plans via
 * {@link TaskPlanner} and are executed sequentially (with optional retry) by
 * {@link TaskExecutor}.
 *
 * <h3>Contract</h3>
 * <ul>
 *   <li>{@link #getName()} must return a stable, human-readable label used in reports.</li>
 *   <li>{@link #execute(WebDriver)} must return a fully built {@link TaskResult}.</li>
 *   <li>Tasks must NOT throw unchecked exceptions out of {@code execute()} — all errors
 *       must be caught internally and reflected in the returned {@link TaskResult}.</li>
 * </ul>
 *
 * Author: Group 5 — Autonomous AI Engine
 */
public interface AutonomousTask {

    /**
     * A stable, human-readable name shown in reports and logs.
     * Example: {@code "Login"}, {@code "Explorer:Repository"}, {@code "Assert:Title"}.
     */
    String getName();

    /**
     * Executes the task and returns a result that always describes what happened.
     *
     * @param driver the active {@link WebDriver} — never null when called by {@link TaskExecutor}
     * @return a non-null {@link TaskResult} with status PASS, FAIL, SKIP, or ERROR
     */
    TaskResult execute(WebDriver driver);

    /**
     * Returns whether this task should be retried on failure.
     * Default: {@code false}.  Override to return {@code true} for flaky tasks.
     */
    default boolean isRetryable() { return false; }

    /**
     * Number of retry attempts allowed when {@link #isRetryable()} is true.
     * Default: 1.
     */
    default int maxRetries() { return 1; }
}
