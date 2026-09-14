package stepdefs;

import ai.ExplorerTask;
import ai.ExplorerTask.ExplorerResult;
import ai.ExplorerTask.RepoSample;
import ai.TaskExecutor;
import ai.TaskPlanner;
import ai.TaskResult;
import ai.reporting.AutonomousReporter;
import driver.DriverManager;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ConfigReader;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExplorerSteps — Cucumber step definitions for the autonomous AI explorer feature.
 *
 * <p>Wires {@code explorer.feature} to the autonomous engine.
 * After each explorer run, the result is stored in {@link #lastResult} and
 * {@link #lastResults} so assertion steps can inspect the full data.
 *
 * Author: Group 5 — Autonomous AI Engine
 */
public class ExplorerSteps {

    private static final Logger LOG = LoggerFactory.getLogger(ExplorerSteps.class);

    /** Full task results from the last autonomous planner run. */
    private List<TaskResult> lastResults;

    /**
     * The structured {@link ExplorerResult} extracted from the last explorer run.
     * Populated by every "the explorer scans …" step via {@link #runExplorer}.
     */
    private ExplorerResult lastResult;

    // ────────────────────────────────────────────────────────────────────────
    //  Autonomous planner (goal DSL) steps
    // ────────────────────────────────────────────────────────────────────────

    @When("the autonomous engine runs the goal {string}")
    public void theAutonomousEngineRunsTheGoal(String goal) {
        LOG.info("  [ExplorerSteps] 🤖 Goal: «{}»", goal);
        TaskPlanner  planner  = new TaskPlanner();
        TaskExecutor executor = new TaskExecutor(DriverManager.getDriver(), false);
        lastResults = executor.execute(planner.plan(goal));
        new AutonomousReporter("CucumberGoal", goal).writeReport(lastResults);
    }

    @Then("the autonomous run should pass")
    public void theAutonomousRunShouldPass() {
        assertNotNull(lastResults, "No tasks were executed — the plan was empty");
        assertFalse(lastResults.isEmpty(), "The task plan produced zero tasks");
        long failed = lastResults.stream().filter(TaskResult::isFailed).count();
        assertEquals(0, failed,
                failed + " task(s) failed:\n" +
                lastResults.stream().map(TaskResult::toString).collect(Collectors.joining("\n")));
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Explorer crawl steps — "start from" variants
    // ────────────────────────────────────────────────────────────────────────

    @When("the explorer scans the GitHub dashboard page")
    public void theExplorerScansTheDashboard() {
        String url = ConfigReader.getProperty("base.url", "https://github.com") + "/";
        runExplorer(url, "DashboardExplorer");
    }

    /**
     * Starts the explorer at the GitHub Explore page (/explore).
     * This page lists repos from many different users — the explorer will
     * visit up to explorer.sample.repos (default 2) of them.
     */
    @When("the explorer scans the GitHub explore page")
    public void theExplorerScansTheExplorePage() {
        String url = ConfigReader.getProperty("base.url", "https://github.com") + "/explore";
        runExplorer(url, "ExplorePageExplorer");
    }

    @When("the explorer scans the repository page for the configured test repo")
    public void theExplorerScansTheRepoPage() {
        String username = ConfigReader.getProperty("github.username", "");
        String repo     = ConfigReader.getProperty("test.repo", "");
        String url      = ConfigReader.getProperty("base.url", "https://github.com")
                          + "/" + username + "/" + repo;
        runExplorer(url, "RepoExplorer");
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Assertion steps
    // ────────────────────────────────────────────────────────────────────────

    @Then("the explorer report should be generated")
    public void explorerReportShouldBeGenerated() {
        // The report is generated as long as the explorer task did not ERROR out
        assertNotNull(lastResults, "Explorer has not run yet");
        boolean hasError = lastResults.stream()
                .anyMatch(r -> r.getStatus() == TaskResult.Status.ERROR);
        assertFalse(hasError,
                "Explorer task ended with ERROR — report may not have been written. " +
                "Check target/ai-reports/ and the console log.");
    }

    @And("the explorer should have visited at least {int} page")
    public void explorerShouldHaveVisitedAtLeastNPages(int min) {
        assertNotNull(lastResult, "No explorer result available");
        int visited = lastResult.visited().size();
        assertTrue(visited >= min,
                "Expected explorer to have visited at least " + min
                + " page(s) but visited " + visited);
        LOG.info("  [ExplorerSteps] ✅ Visited {} page(s)", visited);
    }

    /**
     * Verifies that the explorer visited and recorded at least N repo samples.
     * This assertion is meaningful when starting from /explore or /trending where
     * other users' repos appear in the page listing.
     */
    @And("the explorer should have sampled at least {int} repo")
    public void explorerShouldHaveSampledAtLeastNRepos(int min) {
        assertNotNull(lastResult, "No explorer result available");
        int sampled = lastResult.repoSamples().size();

        // If no repos were sampled, log a clear explanation rather than a hard failure.
        // The explore page may not have rendered repo links in the current session
        // (e.g. first-time visit, A/B test layout, slow React render).
        if (sampled < min) {
            LOG.warn("  [ExplorerSteps] ⚠️  Only {} repo(s) sampled (expected ≥ {}).", sampled, min);
            LOG.warn("  This can happen when the /explore page does not render trending repo links");
            LOG.warn("  in the current session layout. Check ExplorerReport for details.");
        }

        // Soft assertion — warn but do not fail the scenario, because whether GitHub's
        // Explore page renders repo cards is outside our control.
        assertTrue(sampled >= min,
                "Explorer sampled " + sampled + " repo(s) but expected at least " + min + ". "
                + "The /explore page may not have rendered repo links in this session.");
    }

    @And("the sampled repos should have loaded successfully")
    public void sampledReposShouldHaveLoaded() {
        assertNotNull(lastResult, "No explorer result available");
        List<RepoSample> failed = lastResult.repoSamples().stream()
                .filter(rs -> !rs.loaded())
                .toList();

        if (!failed.isEmpty()) {
            String detail = failed.stream()
                    .map(rs -> "  - " + rs.repoUrl() + " → " + rs.pageTitle())
                    .collect(Collectors.joining("\n"));
            fail("The following sampled repos failed to load:\n" + detail);
        }

        LOG.info("  [ExplorerSteps] ✅ All {} sampled repo(s) loaded successfully",
                lastResult.repoSamples().size());

        // Print a summary of what was found in each sampled repo
        lastResult.repoSamples().forEach(rs ->
                LOG.info("  [ExplorerSteps]   🔬 «{}» — {} buttons, {} links",
                         rs.pageTitle(), rs.buttonCount(), rs.linkCount()));
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Internal helper
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Runs the ExplorerTask from {@code startUrl}, stores the raw result list
     * in {@link #lastResults} and extracts the typed {@link ExplorerResult} into
     * {@link #lastResult} for use by assertion steps.
     */
    private void runExplorer(String startUrl, String label) {
        LOG.info("  [ExplorerSteps] 🕷️  Explorer starting from: {}", startUrl);

        ExplorerTask explorer = new ExplorerTask(startUrl);
        TaskExecutor executor = new TaskExecutor(DriverManager.getDriver(), false);
        lastResults = executor.execute(List.of(explorer));

        // Extract the ExplorerResult from the detail lines stored in TaskResult.
        // The ExplorerTask stores typed data internally; we read it back by
        // re-running the task's result details for the step-level assertions,
        // and store a reference via a public accessor added below.
        // For now, we reach into the task directly via a second accessor pattern:
        // we keep the explorer reference and expose its result.
        lastResult = explorer.getResult();

        // Write both the explorer HTML and the task-level autonomous report
        new AutonomousReporter(label, "Explorer crawl from: " + startUrl)
                .writeReport(lastResults);

        LOG.info("  [ExplorerSteps] Explorer done: visited={} repoSamples={} external={} danger={}",
                lastResult.visited().size(),
                lastResult.repoSamples().size(),
                lastResult.externalLinks().size(),
                lastResult.dangerElements().size());
    }
}
