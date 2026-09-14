package ai;

import ai.tasks.AssertionTask;
import ai.tasks.LoginTask;
import ai.tasks.NavigationTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ConfigReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TaskPlanner — converts a high-level goal description into an ordered list of
 * {@link AutonomousTask} instances ready for {@link TaskExecutor} to run.
 *
 * <h3>Goal DSL</h3>
 * Goals are plain strings parsed by keyword matching — no external NLP library needed.
 * The planner reads the goal, identifies intent tokens, and constructs the minimal
 * sequence of tasks that satisfies the intent.
 *
 * <h3>Built-in goal keywords</h3>
 * <pre>
 *   "login"         → LoginTask
 *   "navigate to X" → NavigationTask(X)
 *   "goto X"        → NavigationTask(X)
 *   "open X"        → NavigationTask(X)
 *   "assert X"      → AssertionTask(X)
 *   "check X"       → AssertionTask(X)
 *   "explore"       → ExplorerTask (full page crawl with boundary rules)
 *   "explore X"     → ExplorerTask starting from URL X
 * </pre>
 *
 * <h3>Compound goals</h3>
 * Multiple goals can be chained with "then":
 * <pre>
 *   "login then navigate to /settings then assert url contains settings"
 * </pre>
 *
 * Author: Group 5 — Autonomous AI Engine
 */
public class TaskPlanner {

    private static final Logger LOG = LoggerFactory.getLogger(TaskPlanner.class);

    private final String baseUrl;

    public TaskPlanner() {
        this.baseUrl = ConfigReader.getProperty("base.url", "https://github.com");
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Public API
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Parses {@code goal} and returns the ordered task list.
     * Never returns null — returns an empty list for unrecognised goals.
     *
     * @param goal free-text goal description
     * @return ordered list of tasks to execute
     */
    public List<AutonomousTask> plan(String goal) {
        if (goal == null || goal.isBlank()) {
            LOG.warn("  [Planner] Empty goal — returning empty plan");
            return Collections.emptyList();
        }

        LOG.info("  [Planner] Planning goal: «{}»", goal);
        List<AutonomousTask> plan = new ArrayList<>();

        // Split compound goals on " then " (case-insensitive)
        String[] segments = goal.split("(?i)\\s+then\\s+");
        for (String segment : segments) {
            AutonomousTask task = parseSegment(segment.trim());
            if (task != null) {
                plan.add(task);
                LOG.info("  [Planner]   + {}", task.getName());
            } else {
                LOG.warn("  [Planner]   ? Unrecognised segment: «{}» — skipped", segment.trim());
            }
        }

        LOG.info("  [Planner] Plan ready — {} task(s)", plan.size());
        return Collections.unmodifiableList(plan);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Internal parsing
    // ────────────────────────────────────────────────────────────────────────

    private AutonomousTask parseSegment(String segment) {
        String lower = segment.toLowerCase();

        // ── Login ──────────────────────────────────────────────────────────
        if (lower.equals("login") || lower.contains("log in") || lower.contains("sign in")) {
            return new LoginTask();
        }

        // ── Navigate / goto / open ─────────────────────────────────────────
        if (lower.startsWith("navigate to ") || lower.startsWith("goto ")
                || lower.startsWith("go to ") || lower.startsWith("open ")) {
            String target = extractAfterFirst(segment, " ").trim();
            String url = resolveUrl(target);
            return new NavigationTask(url);
        }

        // ── Assert / check ─────────────────────────────────────────────────
        if (lower.startsWith("assert ") || lower.startsWith("check ")) {
            String expr = extractAfterFirst(segment, " ").trim();
            return new AssertionTask(expr);
        }

        // ── Explore ────────────────────────────────────────────────────────
        if (lower.equals("explore") || lower.startsWith("explore ")) {
            String startUrl = lower.equals("explore")
                    ? baseUrl
                    : resolveUrl(extractAfterFirst(segment, " ").trim());
            return new ExplorerTask(startUrl);
        }

        return null;
    }

    /**
     * Resolves a user-supplied target: if it starts with "http" it is used as-is;
     * if it starts with "/" it is appended to the baseUrl; otherwise it is
     * treated as a relative path on github.com.
     */
    private String resolveUrl(String target) {
        if (target.startsWith("http://") || target.startsWith("https://")) {
            return target;
        }
        String path = target.startsWith("/") ? target : "/" + target;
        return baseUrl + path;
    }

    /** Returns the substring of {@code s} after the first occurrence of {@code sep}. */
    private static String extractAfterFirst(String s, String sep) {
        int idx = s.indexOf(sep);
        return (idx >= 0 && idx < s.length() - 1) ? s.substring(idx + 1) : s;
    }
}
