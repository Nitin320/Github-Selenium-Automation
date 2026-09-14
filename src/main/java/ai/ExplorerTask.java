package ai;

import ai.PageIntelligence.LinkInfo;
import ai.PageIntelligence.PageSnapshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.AdaptiveWait;
import utils.ConfigReader;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * ExplorerTask — autonomously crawls GitHub pages, collecting data about every
 * button, link, and redirect it encounters.
 *
 * <h3>What it does</h3>
 * <ol>
 *   <li>Starts at {@code startUrl} and scans the page with {@link PageIntelligence}.</li>
 *   <li>For every <b>internal</b> github.com link it follows up to {@code maxDepth} hops.</li>
 *   <li>When it finds links to <b>other users' repositories</b> (e.g. repos shown on the
 *       Explore or Trending pages), it visits up to {@code explorer.sample.repos} of them
 *       (default 2), checks that the page loads correctly, and records their data —
 *       then stops at that repo's root without going deeper inside it.</li>
 *   <li>When a link points <b>outside github.com</b> entirely (e.g. a project website),
 *       the redirect is <em>recorded</em> with the exact source page, link text, and
 *       destination URL, but the browser never actually follows it.
 *       This gives the report a clear log of "where would the user leave GitHub".</li>
 *   <li><b>Danger keywords</b> (delete / remove / destroy / archive / disable / revoke) —
 *       the button or link is recorded but never clicked.</li>
 * </ol>
 *
 * <h3>No hard blocking of other users' repos</h3>
 * Unlike the previous design, we do NOT refuse to enter other users' repos.
 * We enter a small sample (1–2) to verify they load, collect their page data,
 * and then stop — exactly what a real user browsing Explore would do.
 *
 * Author: Group 5 — Autonomous AI Engine
 */
public class ExplorerTask implements AutonomousTask {

    private static final Logger LOG = LoggerFactory.getLogger(ExplorerTask.class);

    /** Button / link text patterns that are noted but never acted on. */
    private static final List<String> DANGER_WORDS = List.of(
            "delete", "remove", "destroy", "archive", "disable",
            "revoke", "terminate", "wipe", "purge"
    );

    // ── Configuration ──────────────────────────────────────────────────────
    private final String startUrl;
    private final int    maxDepth;     // how deep to follow own-account pages
    private final int    maxPages;     // hard cap on total pages visited
    private final int    sampleRepos;  // how many other-user repos to spot-check
    private final String ownHost;      // "github.com"
    private final String ownUsername;  // e.g. "group5projectibm"

    // ── Runtime state ──────────────────────────────────────────────────────
    private final Set<String>        visited         = new LinkedHashSet<>();
    private final List<RedirectEntry> externalLinks  = new ArrayList<>(); // recorded, not followed
    private final List<String>        dangerFound    = new ArrayList<>();
    private final List<RepoSample>    repoSamples    = new ArrayList<>(); // other-user repos spot-checked
    private final List<PageSnapshot>  snapshots      = new ArrayList<>();
    private       int                 repoSampleCount = 0;

    public ExplorerTask(String startUrl) {
        this.startUrl    = startUrl;
        this.maxDepth    = Integer.parseInt(ConfigReader.getProperty("explorer.max.depth",   "2"));
        this.maxPages    = Integer.parseInt(ConfigReader.getProperty("explorer.max.pages",   "30"));
        this.sampleRepos = Integer.parseInt(ConfigReader.getProperty("explorer.sample.repos", "2"));
        this.ownHost     = extractHost(ConfigReader.getProperty("base.url", "https://github.com"));
        this.ownUsername = ConfigReader.getProperty("github.username", "").toLowerCase();
    }

    @Override public String  getName()      { return "Explorer:" + startUrl; }
    @Override public boolean isRetryable()  { return false; }

    /**
     * Returns the {@link ExplorerResult} accumulated during the most recent
     * {@link #execute(WebDriver)} call.  Available immediately after execution completes.
     * Returns an empty result if execute has not yet been called.
     */
    public ExplorerResult getResult() {
        return new ExplorerResult(startUrl, visited, externalLinks, dangerFound, repoSamples, snapshots);
    }

    // ──────────────────────────────────────────────────────────────────────
    //  AutonomousTask
    // ──────────────────────────────────────────────────────────────────────

    @Override
    public TaskResult execute(WebDriver driver) {
        Instant start = Instant.now();
        TaskResult.Builder b = TaskResult.builder(getName()).startedAt(start);

        try {
            LOG.info("\n╔══════════════════════════════════════════════════════════════╗");
            LOG.info("  🕷️  EXPLORER  start={}  depth={}  pages={}  sampleRepos={}",
                     startUrl, maxDepth, maxPages, sampleRepos);
            LOG.info("╚══════════════════════════════════════════════════════════════╝");

            crawl(driver, startUrl, 0, false);

            // ── Summary ────────────────────────────────────────────────────
            b.status(TaskResult.Status.PASS)
             .detail("Pages visited (own account + special pages): " + visited.size())
             .detail("Other-user repos spot-checked: " + repoSamples.size())
             .detail("External links recorded (not followed): " + externalLinks.size())
             .detail("Dangerous elements noted (not clicked): " + dangerFound.size());

            // List every external link with its source
            externalLinks.forEach(e ->
                b.detail(String.format("  [REDIRECT] %-50s  →  %s  (from: %s)",
                         e.linkText(), e.destUrl(), e.sourcePageUrl())));

            // List sampled repos
            repoSamples.forEach(rs ->
                b.detail(String.format("  [REPO-SAMPLE] %s  —  loaded=%s  buttons=%d  links=%d",
                         rs.repoUrl(), rs.loaded(), rs.buttonCount(), rs.linkCount())));

            // List danger
            dangerFound.forEach(d -> b.detail("  [DANGER] " + d));

            // Write HTML report
            new ExplorerReportWriter().write(new ExplorerResult(
                    startUrl, visited, externalLinks, dangerFound, repoSamples, snapshots));

            LOG.info("  🕷️  EXPLORER done — visited={} repos-sampled={} external={} danger={}",
                     visited.size(), repoSamples.size(), externalLinks.size(), dangerFound.size());

        } catch (Exception e) {
            LOG.error("  [Explorer] Fatal: {}", e.getMessage(), e);
            b.error(e);
        }

        return b.finishedAt(Instant.now()).build();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Core crawl loop
    // ──────────────────────────────────────────────────────────────────────

    /**
     * @param isSampleRepo  true when this call is a one-level spot-check of
     *                      another user's repo — no further recursion from here
     */
    private void crawl(WebDriver driver, String url, int depth, boolean isSampleRepo) {
        if (visited.size() >= maxPages) return;
        if (depth > maxDepth && !isSampleRepo) return;
        if (visited.contains(url)) return;

        visited.add(url);
        LOG.info("  [Explorer] [depth={}{}] ▶ {}", depth, isSampleRepo ? "/sample" : "", url);

        try {
            driver.get(url);
            // Wait for the page to reach readyState=complete
            AdaptiveWait.waitFor(driver,
                    d -> "complete".equals(
                            ((org.openqa.selenium.JavascriptExecutor) d)
                                    .executeScript("return document.readyState")),
                    "explorerPageLoad");

            // Detect if GitHub actually redirected us somewhere unexpected
            String landedUrl = driver.getCurrentUrl();
            if (!landedUrl.equals(url)) {
                LOG.info("  [Explorer]   ↪ Redirected to: {}", landedUrl);
            }

            PageIntelligence intel   = new PageIntelligence(driver, ownHost);
            PageSnapshot     snap    = intel.snapshot();
            snapshots.add(snap);

            logSnapshot(snap, depth);

            // ── Record danger elements ─────────────────────────────────────
            snap.getButtons().forEach(btn -> {
                if (isDangerous(btn.label())) {
                    dangerFound.add("[" + snap.getUrl() + "] Button: «" + btn.label() + "»");
                    LOG.warn("  [Explorer]   ⚠️  Danger button (not clicked): {}", btn.label());
                }
            });
            snap.getLinks().forEach(link -> {
                if (isDangerous(link.text())) {
                    dangerFound.add("[" + snap.getUrl() + "] Link: «" + link.text() + "» → " + link.href());
                    LOG.warn("  [Explorer]   ⚠️  Danger link (not followed): {}", link.text());
                }
            });

            // ── Record external links — log detail but don't follow ────────
            snap.getExternalLinks().forEach(link -> {
                RedirectEntry entry = new RedirectEntry(snap.getUrl(), link.text(), link.href());
                // De-duplicate by destination URL
                boolean alreadySeen = externalLinks.stream()
                        .anyMatch(e -> e.destUrl().equals(link.href()));
                if (!alreadySeen) {
                    externalLinks.add(entry);
                    LOG.info("  [Explorer]   🌐 External link: «{}» → {}  (would leave GitHub)",
                             link.text().isBlank() ? link.href() : link.text(), link.href());
                }
            });

            // ── If this is a sample-repo spot-check, record and return ──────
            if (isSampleRepo) {
                repoSamples.add(new RepoSample(
                        url, true, snap.getButtons().size(), snap.getLinks().size(),
                        snap.getTitle()));
                LOG.info("  [Explorer]   ✅ Repo sample recorded: «{}»", snap.getTitle());
                return; // do not recurse inside other people's repos
            }

            // ── Recurse: internal links on own-account / special pages ──────
            if (depth < maxDepth) {
                for (LinkInfo link : snap.getInternalLinks()) {
                    if (isDangerous(link.text())) continue;
                    if (visited.contains(link.href())) continue;

                    LinkKind kind = classifyInternalLink(link.href());

                    if (kind == LinkKind.OWN_OR_SPECIAL) {
                        // Follow normally
                        crawl(driver, link.href(), depth + 1, false);

                    } else if (kind == LinkKind.OTHER_USER_REPO
                               && repoSampleCount < sampleRepos) {
                        // Spot-check this repo: visit its root page only
                        repoSampleCount++;
                        LOG.info("  [Explorer]   🔬 Sampling repo ({}/{}): {}",
                                 repoSampleCount, sampleRepos, link.href());
                        crawl(driver, link.href(), depth + 1, true);
                    }
                    // else: OTHER_USER_REPO beyond sample limit → skip silently

                    if (visited.size() >= maxPages) break;
                }
            }

        } catch (Exception e) {
            LOG.warn("  [Explorer]   ❌ Error on {}: {}", url, e.getMessage());
            // If this was a sample-repo that failed to load, still record it
            if (isSampleRepo) {
                repoSamples.add(new RepoSample(url, false, 0, 0, "(failed to load)"));
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Link classification
    // ──────────────────────────────────────────────────────────────────────

    private enum LinkKind { OWN_OR_SPECIAL, OTHER_USER_REPO }

    /**
     * Decides whether a github.com-internal link belongs to the test account /
     * a GitHub special page, or to another user's repo.
     */
    private LinkKind classifyInternalLink(String url) {
        try {
            java.net.URI uri = java.net.URI.create(url);
            String path = uri.getPath();
            if (path == null || path.length() <= 1) return LinkKind.OWN_OR_SPECIAL;

            // Split into ["", "owner", "repo", "..."]
            String[] parts = path.split("/", 4);
            if (parts.length < 2) return LinkKind.OWN_OR_SPECIAL;

            String firstSegment = parts[1].toLowerCase();

            // GitHub special pages — always safe to follow
            if (GITHUB_SPECIAL_PATHS.contains(firstSegment)) return LinkKind.OWN_OR_SPECIAL;

            // Own username — safe
            if (firstSegment.equals(ownUsername)) return LinkKind.OWN_OR_SPECIAL;

            // Anything else is another user's content
            return LinkKind.OTHER_USER_REPO;

        } catch (Exception e) {
            return LinkKind.OWN_OR_SPECIAL;
        }
    }

    /** GitHub paths that are not user-owned repos. */
    private static final Set<String> GITHUB_SPECIAL_PATHS = Set.of(
            "explore", "trending", "search", "marketplace", "features", "about",
            "login", "logout", "settings", "notifications", "new", "orgs", "gist",
            "pulls", "issues", "dashboard", "codespaces", "sponsors", "pricing",
            "enterprise", "topics", "collections", "events", "readme"
    );

    private boolean isDangerous(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        return DANGER_WORDS.stream().anyMatch(lower::contains);
    }

    private static String extractHost(String url) {
        try { return java.net.URI.create(url).getHost(); }
        catch (Exception e) { return url; }
    }

    private void logSnapshot(PageSnapshot snap, int depth) {
        String indent = "  ".repeat(depth + 2);
        LOG.info("{}📄 «{}»  links={}  buttons={}",
                 indent, snap.getTitle(), snap.getLinks().size(), snap.getButtons().size());
        snap.getButtons().stream()
            .filter(b -> !b.label().isBlank() && !b.label().equals("(unlabelled)"))
            .forEach(b -> LOG.info("{}  🔘 {}", indent, b.label()));
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Data records
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Records one external link that was found but not followed.
     *
     * @param sourcePageUrl  the github.com page that contained this link
     * @param linkText       the visible anchor text
     * @param destUrl        the full destination URL outside github.com
     */
    public record RedirectEntry(String sourcePageUrl, String linkText, String destUrl) {}

    /**
     * Records the result of visiting another user's repository as a sample.
     *
     * @param repoUrl     URL visited
     * @param loaded      true if the page loaded without error
     * @param buttonCount number of visible buttons found
     * @param linkCount   number of links found
     * @param pageTitle   document title of the repo page
     */
    public record RepoSample(String repoUrl, boolean loaded,
                             int buttonCount, int linkCount, String pageTitle) {}

    /** Immutable summary of a complete crawl run. */
    public record ExplorerResult(
            String            startUrl,
            Set<String>       visited,
            List<RedirectEntry> externalLinks,
            List<String>      dangerElements,
            List<RepoSample>  repoSamples,
            List<PageSnapshot> snapshots) {}

    // ──────────────────────────────────────────────────────────────────────
    //  HTML Report writer (inner class — no extra dependency)
    // ──────────────────────────────────────────────────────────────────────

    private static final class ExplorerReportWriter {

        void write(ExplorerResult r) {
            String ts  = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String dir = ConfigReader.getProperty("ai.reports.dir", "target/ai-reports");
            java.nio.file.Path path = java.nio.file.Path.of(dir, "ExplorerReport_" + ts + ".html");

            try {
                java.nio.file.Files.createDirectories(path.getParent());
                try (java.io.PrintWriter w = new java.io.PrintWriter(
                        java.nio.file.Files.newBufferedWriter(
                                path, java.nio.charset.StandardCharsets.UTF_8))) {

                    w.println("<!DOCTYPE html><html lang='en'><head>");
                    w.println("<meta charset='UTF-8'>");
                    w.println("<title>Explorer Report — " + ts + "</title>");
                    w.println("<style>" + CSS + "</style></head><body>");

                    // Header
                    w.println("<div class='header'>");
                    w.println("<h1>🕷️ Autonomous Explorer Report</h1>");
                    w.println("<div class='meta'>Started from: <code>" + esc(r.startUrl()) + "</code></div>");
                    w.println("</div>");

                    // Summary cards
                    w.println("<div class='summary'>");
                    w.println(card("Pages Visited",       r.visited().size(),        "blue"));
                    w.println(card("Repos Sampled",        r.repoSamples().size(),    "green"));
                    w.println(card("External Links Found", r.externalLinks().size(),  "yellow"));
                    w.println(card("Danger Elements",      r.dangerElements().size(), "red"));
                    w.println("</div>");

                    // ── Pages Visited ─────────────────────────────────────────
                    section(w, "✅ Pages Visited (" + r.visited().size() + ")", () -> {
                        w.println("<ul class='url-list green'>");
                        r.visited().forEach(u -> w.println("<li>" + esc(u) + "</li>"));
                        w.println("</ul>");
                    });

                    // ── Repo Samples ──────────────────────────────────────────
                    section(w, "🔬 Other-User Repos Sampled (" + r.repoSamples().size() + ")", () -> {
                        if (r.repoSamples().isEmpty()) {
                            w.println("<p class='empty'>No repos were sampled on this run.</p>");
                            return;
                        }
                        w.println("<table><thead><tr>");
                        w.println("<th>Repo URL</th><th>Page Title</th><th>Loaded?</th><th>Buttons</th><th>Links</th>");
                        w.println("</tr></thead><tbody>");
                        r.repoSamples().forEach(rs -> {
                            w.println("<tr>");
                            w.println("<td class='mono'><a href='" + esc(rs.repoUrl()) + "' target='_blank'>"
                                    + esc(rs.repoUrl()) + "</a></td>");
                            w.println("<td>" + esc(rs.pageTitle()) + "</td>");
                            w.println("<td>" + (rs.loaded()
                                    ? "<span class='badge green'>YES</span>"
                                    : "<span class='badge red'>NO</span>") + "</td>");
                            w.println("<td>" + rs.buttonCount() + "</td>");
                            w.println("<td>" + rs.linkCount() + "</td>");
                            w.println("</tr>");
                        });
                        w.println("</tbody></table>");
                    });

                    // ── External Links ────────────────────────────────────────
                    section(w, "🌐 External Links — Where GitHub Would Redirect ("
                               + r.externalLinks().size() + ")", () -> {
                        if (r.externalLinks().isEmpty()) {
                            w.println("<p class='empty'>No external links found.</p>");
                            return;
                        }
                        w.println("<table><thead><tr>");
                        w.println("<th>Link Text</th><th>Destination (outside github.com)</th><th>Found on Page</th>");
                        w.println("</tr></thead><tbody>");
                        r.externalLinks().forEach(e -> {
                            w.println("<tr>");
                            w.println("<td>" + esc(e.linkText().isBlank() ? "(no text)" : e.linkText()) + "</td>");
                            w.println("<td class='mono yellow'><a href='" + esc(e.destUrl()) + "' target='_blank'>"
                                    + esc(e.destUrl()) + "</a></td>");
                            w.println("<td class='mono small'>" + esc(e.sourcePageUrl()) + "</td>");
                            w.println("</tr>");
                        });
                        w.println("</tbody></table>");
                    });

                    // ── Danger Elements ───────────────────────────────────────
                    section(w, "⚠️ Dangerous Elements Found — Not Clicked ("
                               + r.dangerElements().size() + ")", () -> {
                        if (r.dangerElements().isEmpty()) {
                            w.println("<p class='empty'>None found.</p>");
                            return;
                        }
                        w.println("<ul class='url-list red'>");
                        r.dangerElements().forEach(d -> w.println("<li>" + esc(d) + "</li>"));
                        w.println("</ul>");
                    });

                    // ── Per-page element inventory ────────────────────────────
                    section(w, "📋 Per-Page Element Inventory (" + r.snapshots().size() + " pages)", () -> {
                        r.snapshots().forEach(snap -> {
                            w.println("<div class='page-card'>");
                            w.println("<div class='page-url'>" + esc(snap.getUrl()) + "</div>");
                            w.println("<div class='page-title'>" + esc(snap.getTitle()) + "</div>");

                            if (!snap.getButtons().isEmpty()) {
                                w.println("<div class='label'>Buttons (" + snap.getButtons().size() + ")</div><ul>");
                                snap.getButtons().stream()
                                    .filter(b -> !b.label().equals("(unlabelled)"))
                                    .forEach(b -> w.println("<li class='btn'>" + esc(b.label()) + "</li>"));
                                w.println("</ul>");
                            }

                            List<ai.PageIntelligence.LinkInfo> intLinks = snap.getInternalLinks();
                            if (!intLinks.isEmpty()) {
                                w.println("<div class='label'>Internal Links (" + intLinks.size() + ")</div><ul>");
                                intLinks.stream().limit(15).forEach(l ->
                                        w.println("<li><a href='" + esc(l.href()) + "' target='_blank'>"
                                                + esc(l.text().isBlank() ? l.href() : l.text()) + "</a></li>"));
                                if (intLinks.size() > 15)
                                    w.println("<li class='muted'>… and " + (intLinks.size()-15) + " more</li>");
                                w.println("</ul>");
                            }
                            w.println("</div>");
                        });
                    });

                    w.println("<footer>Made with IBM Bob — Autonomous Explorer — Group 5</footer>");
                    w.println("</body></html>");
                }
                LOG.info("  🕷️  Explorer report → {}", path.toAbsolutePath());

            } catch (Exception e) {
                LOG.error("  ❌ Could not write explorer report: {}", e.getMessage());
            }
        }

        @FunctionalInterface interface Block { void run(); }

        private void section(java.io.PrintWriter w, String title, Block body) {
            w.println("<div class='section'>");
            w.println("<h2 class='section-title'>" + title + "</h2>");
            w.println("<div class='section-body'>");
            body.run();
            w.println("</div></div>");
        }

        private String card(String label, int value, String colour) {
            return "<div class='card " + colour + "'><span class='num'>" + value + "</span>"
                 + "<span class='lbl'>" + label + "</span></div>";
        }

        private static String esc(String s) {
            if (s == null) return "";
            return s.replace("&","&amp;").replace("<","&lt;")
                    .replace(">","&gt;").replace("\"","&quot;");
        }

        private static final String CSS = """
            *,*::before,*::after{box-sizing:border-box;margin:0;padding:0}
            body{font-family:-apple-system,"Segoe UI",system-ui,sans-serif;font-size:14px;
                 line-height:1.6;background:#0d1117;color:#c9d1d9}
            .header{background:#161b22;border-bottom:1px solid #30363d;padding:20px 32px}
            .header h1{font-size:20px;color:#f0f6fc;margin-bottom:4px}
            .meta{color:#8b949e;font-size:12px}.meta code{background:#21262d;
                 padding:1px 6px;border-radius:3px;color:#79c0ff;font-size:11px}
            .summary{display:flex;gap:14px;padding:18px 32px;background:#161b22;
                     border-bottom:1px solid #30363d;flex-wrap:wrap}
            .card{background:#21262d;border:1px solid #30363d;border-radius:6px;
                  padding:12px 18px;min-width:140px;text-align:center}
            .card .num{display:block;font-size:28px;font-weight:700;margin-bottom:2px}
            .card .lbl{font-size:11px;color:#8b949e;text-transform:uppercase;letter-spacing:.04em}
            .card.blue  .num{color:#58a6ff}
            .card.green .num{color:#3fb950}
            .card.yellow.num{color:#d29922}
            .card.red   .num{color:#f85149}
            .section{border-bottom:1px solid #21262d}
            .section-title{padding:14px 32px 6px;font-size:14px;font-weight:600;color:#f0f6fc}
            .section-body{padding:0 32px 16px}
            .url-list{list-style:none;padding:8px 0 0}
            .url-list li{padding:3px 0;font-size:13px;border-bottom:1px solid #21262d}
            .url-list.green li{color:#3fb950}
            .url-list.red   li{color:#f85149}
            .empty{color:#8b949e;font-size:13px;padding:6px 0}
            table{width:100%;border-collapse:collapse;margin-top:8px;font-size:13px}
            th{background:#161b22;padding:8px 12px;text-align:left;
               border-bottom:2px solid #30363d;font-size:11px;text-transform:uppercase;
               color:#8b949e;letter-spacing:.04em}
            td{padding:7px 12px;border-bottom:1px solid #21262d;vertical-align:top}
            .mono{font-family:monospace;font-size:12px}
            .small{font-size:11px;color:#8b949e}
            .yellow{color:#d29922}
            .badge{display:inline-block;padding:1px 8px;border-radius:10px;
                   font-size:11px;font-weight:600}
            .badge.green{background:#1a3a1a;color:#3fb950}
            .badge.red  {background:#3a1a1a;color:#f85149}
            a{color:#58a6ff;text-decoration:none}
            .page-card{margin:10px 0;background:#161b22;border:1px solid #30363d;
                       border-radius:6px;padding:12px 16px}
            .page-url{font-family:monospace;font-size:11px;color:#79c0ff;margin-bottom:3px}
            .page-title{font-size:13px;color:#f0f6fc;margin-bottom:6px}
            .label{font-size:10px;text-transform:uppercase;color:#8b949e;
                   letter-spacing:.04em;margin-top:8px;margin-bottom:3px}
            .page-card ul{list-style:none;padding-left:8px}
            .page-card li{font-size:12px;padding:2px 0;color:#8b949e}
            .btn{color:#d29922}.muted{color:#57606a}
            footer{padding:16px 32px;color:#57606a;font-size:12px;
                   border-top:1px solid #21262d;text-align:center}
            """;
    }
}
