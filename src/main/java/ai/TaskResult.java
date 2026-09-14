package ai;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TaskResult — immutable record of what happened when one {@link AutonomousTask} ran.
 *
 * <p>Every task, whether it passed, failed, or was skipped, produces exactly one
 * {@code TaskResult}.  The {@link ai.reporting.AutonomousReporter} consumes these
 * to build the final HTML + JSON report.
 *
 * <h3>Fields</h3>
 * <ul>
 *   <li>{@code taskName}   — human-readable label (e.g. "Login", "Explorer:Dashboard")</li>
 *   <li>{@code status}     — PASS / FAIL / SKIP / ERROR</li>
 *   <li>{@code startedAt} / {@code finishedAt} — wall-clock instants for timing</li>
 *   <li>{@code details}    — ordered log lines emitted during execution</li>
 *   <li>{@code screenshotPath} — relative path of failure screenshot if captured</li>
 *   <li>{@code cause}      — the throwable that caused FAIL/ERROR, may be null</li>
 * </ul>
 *
 * Author: Group 5 — Autonomous AI Engine
 */
public final class TaskResult {

    // ── Status enum ──────────────────────────────────────────────────────────

    public enum Status { PASS, FAIL, SKIP, ERROR }

    // ── Fields ───────────────────────────────────────────────────────────────

    private final String       taskName;
    private final Status       status;
    private final Instant      startedAt;
    private final Instant      finishedAt;
    private final List<String> details;
    private final String       screenshotPath;
    private final Throwable    cause;

    // ── Constructor (use Builder) ─────────────────────────────────────────────

    private TaskResult(Builder b) {
        this.taskName       = b.taskName;
        this.status         = b.status;
        this.startedAt      = b.startedAt;
        this.finishedAt     = b.finishedAt;
        this.details        = Collections.unmodifiableList(new ArrayList<>(b.details));
        this.screenshotPath = b.screenshotPath;
        this.cause          = b.cause;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public String       getTaskName()       { return taskName; }
    public Status       getStatus()         { return status; }
    public Instant      getStartedAt()      { return startedAt; }
    public Instant      getFinishedAt()     { return finishedAt; }
    public List<String> getDetails()        { return details; }
    public String       getScreenshotPath() { return screenshotPath; }
    public Throwable    getCause()          { return cause; }

    public boolean isPassed()  { return status == Status.PASS; }
    public boolean isFailed()  { return status == Status.FAIL || status == Status.ERROR; }

    /** Wall-clock duration of the task execution. */
    public Duration getDuration() {
        if (startedAt == null || finishedAt == null) return Duration.ZERO;
        return Duration.between(startedAt, finishedAt);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%dms)", status, taskName, getDuration().toMillis());
    }

    // ── Static factories ──────────────────────────────────────────────────────

    public static Builder builder(String taskName) { return new Builder(taskName); }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static final class Builder {

        private final String       taskName;
        private       Status       status       = Status.PASS;
        private       Instant      startedAt    = Instant.now();
        private       Instant      finishedAt   = Instant.now();
        private final List<String> details      = new ArrayList<>();
        private       String       screenshotPath;
        private       Throwable    cause;

        private Builder(String taskName) {
            this.taskName = taskName;
        }

        public Builder status(Status s)              { this.status = s;             return this; }
        public Builder startedAt(Instant t)          { this.startedAt = t;          return this; }
        public Builder finishedAt(Instant t)         { this.finishedAt = t;         return this; }
        public Builder detail(String line)           { this.details.add(line);      return this; }
        public Builder details(List<String> lines)   { this.details.addAll(lines);  return this; }
        public Builder screenshotPath(String path)   { this.screenshotPath = path;  return this; }
        public Builder cause(Throwable t)            { this.cause = t;              return this; }

        public Builder fail(String reason) {
            this.status = Status.FAIL;
            this.details.add("FAIL: " + reason);
            return this;
        }

        public Builder fail(String reason, Throwable t) {
            this.status = Status.FAIL;
            this.cause  = t;
            this.details.add("FAIL: " + reason + " — " + t.getMessage());
            return this;
        }

        public Builder error(Throwable t) {
            this.status = Status.ERROR;
            this.cause  = t;
            this.details.add("ERROR: " + t.getClass().getSimpleName() + ": " + t.getMessage());
            return this;
        }

        public TaskResult build() {
            if (finishedAt == null) finishedAt = Instant.now();
            return new TaskResult(this);
        }
    }
}
