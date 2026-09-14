package ai.reporting;

import ai.TaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ConfigReader;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * AutonomousReporter — writes a self-contained HTML dashboard and a machine-readable
 * JSON file after every autonomous task run.
 *
 * <h3>Outputs</h3>
 * <ul>
 *   <li>{@code target/ai-reports/AutonomousReport_<timestamp>.html}
 *       — a colour-coded HTML page showing every task result with timing,
 *         log details, and an inline screenshot link when available.</li>
 *   <li>{@code target/ai-reports/AutonomousReport_<timestamp>.json}
 *       — a compact JSON array of task results for downstream tooling
 *         (e.g. Allure custom executor, dashboard ingest).</li>
 * </ul>
 *
 * <h3>Design</h3>
 * Pure Java with zero additional dependencies — everything is rendered by hand
 * so the output is portable across all environments without requiring a template
 * engine on the classpath.
 *
 * Author: Group 5 — Autonomous AI Engine
 */
public class AutonomousReporter {

    private static final Logger LOG = LoggerFactory.getLogger(AutonomousReporter.class);

    private static final DateTimeFormatter TS_FILE =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter TS_HUMAN =
            DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");

    private final String runLabel;
    private final String goal;

    /**
     * @param runLabel short label for the run, e.g. "ExplorerRun" or "LoginSuite"
     * @param goal     the original goal string passed to {@link ai.TaskPlanner}
     */
    public AutonomousReporter(String runLabel, String goal) {
        this.runLabel = runLabel;
        this.goal     = goal;
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Public API
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Writes the HTML and JSON reports to {@code target/ai-reports/}.
     *
     * @param results the ordered list of task results returned by {@link ai.TaskExecutor}
     */
    public void writeReport(List<TaskResult> results) {
        String timestamp = LocalDateTime.now().format(TS_FILE);
        String dir = ConfigReader.getProperty("ai.reports.dir", "target/ai-reports");

        try {
            Path outDir = Path.of(dir);
            Files.createDirectories(outDir);

            Path htmlPath = outDir.resolve("AutonomousReport_" + timestamp + ".html");
            Path jsonPath = outDir.resolve("AutonomousReport_" + timestamp + ".json");

            writeHtml(htmlPath, results, timestamp);
            writeJson(jsonPath, results, timestamp);

            LOG.info("  📊 Autonomous HTML report → {}", htmlPath.toAbsolutePath());
            LOG.info("  📋 Autonomous JSON report → {}", jsonPath.toAbsolutePath());

        } catch (IOException e) {
            LOG.error("  ❌ Failed to write autonomous reports: {}", e.getMessage());
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  HTML generation
    // ────────────────────────────────────────────────────────────────────────

    private void writeHtml(Path path, List<TaskResult> results, String timestamp) throws IOException {
        long passed  = results.stream().filter(TaskResult::isPassed).count();
        long failed  = results.stream().filter(TaskResult::isFailed).count();
        long skipped = results.stream()
                .filter(r -> r.getStatus() == TaskResult.Status.SKIP).count();
        long totalMs = results.stream().mapToLong(r -> r.getDuration().toMillis()).sum();
        String humanTime = LocalDateTime.now().format(TS_HUMAN);

        try (PrintWriter w = new PrintWriter(
                Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {

            w.println("<!DOCTYPE html>");
            w.println("<html lang='en'><head>");
            w.println("<meta charset='UTF-8'>");
            w.println("<meta name='viewport' content='width=device-width,initial-scale=1'>");
            w.println("<title>Autonomous Report — " + esc(runLabel) + "</title>");
            w.println("<style>");
            w.println(CSS);
            w.println("</style></head><body>");

            // ── Header ──────────────────────────────────────────────────────
            w.println("<div class='header'>");
            w.println("  <h1>🤖 Autonomous Test Report</h1>");
            w.println("  <div class='meta'>Run: <b>" + esc(runLabel) + "</b> &nbsp;|&nbsp; "
                    + humanTime + "</div>");
            w.println("  <div class='goal'>Goal: <code>" + esc(goal) + "</code></div>");
            w.println("</div>");

            // ── Summary bar ─────────────────────────────────────────────────
            w.println("<div class='summary'>");
            w.println("  <div class='stat stat-pass'><span>" + passed + "</span>PASSED</div>");
            w.println("  <div class='stat stat-fail'><span>" + failed + "</span>FAILED</div>");
            w.println("  <div class='stat stat-skip'><span>" + skipped + "</span>SKIPPED</div>");
            w.println("  <div class='stat stat-time'><span>" + totalMs + "ms</span>TOTAL TIME</div>");
            w.println("</div>");

            // ── Task table ───────────────────────────────────────────────────
            w.println("<table>");
            w.println("<thead><tr><th>#</th><th>Task</th><th>Status</th>"
                    + "<th>Duration</th><th>Details</th><th>Screenshot</th></tr></thead>");
            w.println("<tbody>");

            for (int i = 0; i < results.size(); i++) {
                TaskResult r = results.get(i);
                String rowClass = switch (r.getStatus()) {
                    case PASS  -> "row-pass";
                    case FAIL, ERROR -> "row-fail";
                    case SKIP  -> "row-skip";
                };
                String badge = switch (r.getStatus()) {
                    case PASS  -> "<span class='badge badge-pass'>PASS</span>";
                    case FAIL  -> "<span class='badge badge-fail'>FAIL</span>";
                    case ERROR -> "<span class='badge badge-error'>ERROR</span>";
                    case SKIP  -> "<span class='badge badge-skip'>SKIP</span>";
                };
                String screenshot = r.getScreenshotPath() != null
                        ? "<a href='" + esc(r.getScreenshotPath()) + "' target='_blank'>📸 View</a>"
                        : "—";
                String details = "<ul class='detail-list'>"
                        + r.getDetails().stream()
                              .map(d -> "<li>" + esc(d) + "</li>")
                              .collect(java.util.stream.Collectors.joining())
                        + "</ul>";

                w.println("<tr class='" + rowClass + "'>");
                w.println("  <td>" + (i + 1) + "</td>");
                w.println("  <td class='task-name'>" + esc(r.getTaskName()) + "</td>");
                w.println("  <td>" + badge + "</td>");
                w.println("  <td>" + r.getDuration().toMillis() + "ms</td>");
                w.println("  <td>" + details + "</td>");
                w.println("  <td>" + screenshot + "</td>");
                w.println("</tr>");
            }

            w.println("</tbody></table>");
            w.println("<footer>Made with IBM Bob &mdash; Autonomous AI Engine &mdash; Group 5</footer>");
            w.println("</body></html>");
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  JSON generation
    // ────────────────────────────────────────────────────────────────────────

    private void writeJson(Path path, List<TaskResult> results, String timestamp) throws IOException {
        try (PrintWriter w = new PrintWriter(
                Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {

            w.println("{");
            w.println("  \"runLabel\": \"" + jsonEsc(runLabel) + "\",");
            w.println("  \"goal\": \"" + jsonEsc(goal) + "\",");
            w.println("  \"generatedAt\": \"" + timestamp + "\",");
            w.println("  \"summary\": {");
            long passed  = results.stream().filter(TaskResult::isPassed).count();
            long failed  = results.stream().filter(TaskResult::isFailed).count();
            long skipped = results.stream()
                    .filter(r -> r.getStatus() == TaskResult.Status.SKIP).count();
            w.println("    \"total\": " + results.size() + ",");
            w.println("    \"passed\": " + passed + ",");
            w.println("    \"failed\": " + failed + ",");
            w.println("    \"skipped\": " + skipped);
            w.println("  },");
            w.println("  \"tasks\": [");

            for (int i = 0; i < results.size(); i++) {
                TaskResult r = results.get(i);
                w.println("    {");
                w.println("      \"name\": \"" + jsonEsc(r.getTaskName()) + "\",");
                w.println("      \"status\": \"" + r.getStatus() + "\",");
                w.println("      \"durationMs\": " + r.getDuration().toMillis() + ",");
                String startIso = r.getStartedAt() != null
                        ? r.getStartedAt().atZone(ZoneId.systemDefault())
                                          .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        : "";
                w.println("      \"startedAt\": \"" + startIso + "\",");
                w.println("      \"screenshotPath\": "
                        + (r.getScreenshotPath() != null
                            ? "\"" + jsonEsc(r.getScreenshotPath()) + "\""
                            : "null") + ",");
                // Details array
                w.print("      \"details\": [");
                List<String> details = r.getDetails();
                for (int j = 0; j < details.size(); j++) {
                    w.print("\"" + jsonEsc(details.get(j)) + "\"");
                    if (j < details.size() - 1) w.print(", ");
                }
                w.println("]");
                w.print("    }");
                if (i < results.size() - 1) w.println(",");
                else w.println();
            }

            w.println("  ]");
            w.println("}");
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ────────────────────────────────────────────────────────────────────────

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static String jsonEsc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Inline CSS
    // ────────────────────────────────────────────────────────────────────────

    private static final String CSS = """
        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
        body {
          font-family: -apple-system, "Segoe UI", system-ui, sans-serif;
          font-size: 14px; line-height: 1.6;
          background: #0d1117; color: #c9d1d9;
        }
        .header {
          background: #161b22; border-bottom: 1px solid #30363d;
          padding: 20px 32px;
        }
        .header h1 { font-size: 22px; color: #f0f6fc; margin-bottom: 6px; }
        .meta, .goal { color: #8b949e; font-size: 13px; }
        .goal { margin-top: 4px; }
        .goal code {
          background: #21262d; padding: 2px 6px; border-radius: 4px;
          font-size: 12px; color: #79c0ff;
        }
        .summary {
          display: flex; gap: 16px;
          padding: 20px 32px; background: #161b22;
          border-bottom: 1px solid #30363d;
        }
        .stat {
          background: #21262d; border: 1px solid #30363d;
          border-radius: 6px; padding: 12px 20px; text-align: center; min-width: 120px;
        }
        .stat span { display: block; font-size: 28px; font-weight: 700; }
        .stat { font-size: 11px; color: #8b949e; text-transform: uppercase; letter-spacing: .04em; }
        .stat-pass span { color: #3fb950; }
        .stat-fail span { color: #f85149; }
        .stat-skip span { color: #d29922; }
        .stat-time span { color: #58a6ff; }
        table {
          width: 100%; border-collapse: collapse; margin: 0;
        }
        th {
          background: #161b22; padding: 10px 16px; text-align: left;
          border-bottom: 2px solid #30363d; font-size: 12px;
          text-transform: uppercase; letter-spacing: .04em; color: #8b949e;
        }
        td {
          padding: 10px 16px; border-bottom: 1px solid #21262d;
          vertical-align: top;
        }
        .row-pass td { border-left: 3px solid #3fb950; }
        .row-fail td { border-left: 3px solid #f85149; }
        .row-skip td { border-left: 3px solid #d29922; }
        .task-name { font-family: monospace; font-size: 13px; color: #79c0ff; max-width: 320px; word-break: break-all; }
        .badge {
          display: inline-block; padding: 2px 10px; border-radius: 10px;
          font-size: 11px; font-weight: 600;
        }
        .badge-pass  { background: #1a3a1a; color: #3fb950; }
        .badge-fail  { background: #3a1a1a; color: #f85149; }
        .badge-error { background: #3a1a2a; color: #ff7b72; }
        .badge-skip  { background: #3a2e1a; color: #d29922; }
        .detail-list { list-style: none; padding: 0; }
        .detail-list li { font-size: 12px; color: #8b949e; padding: 1px 0; }
        table a { color: #58a6ff; }
        footer {
          padding: 20px 32px; color: #57606a; font-size: 12px;
          border-top: 1px solid #21262d; text-align: center;
        }
        """;
}
