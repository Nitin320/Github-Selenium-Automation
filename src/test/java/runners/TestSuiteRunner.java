package runners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * TestSuiteRunner — JUnit Platform Suite runner for Cucumber features.
 *
 * <p>All Cucumber configuration (glue, plugin, feature path) lives HERE,
 * scoped with {@code @ConfigurationParameter}, instead of in the global
 * {@code junit-platform.properties} file. This is deliberate: when
 * {@code cucumber.features} is set globally, the Cucumber JUnit Platform
 * Engine ignores whatever specific class you asked to run and always
 * re-discovers every scenario under that path — which is exactly why
 * running LoginTest, LogoutTest, or ProfileTest individually was instead
 * executing all of login.feature every time.
 *
 * <p>With the config declared only here, Cucumber only activates when
 * THIS class is the thing you run (e.g. via {@code mvn test}, or by
 * right-clicking TestSuiteRunner in the IDE). Plain JUnit 5 classes are
 * left alone and run normally.
 *
 * Author: Nitheesh
 */
/**
 * Tag-based filtering:
 *
 *   Run only smoke tests:
 *     mvn test -Dcucumber.filter.tags="@smoke"
 *
 *   Run only regression tests:
 *     mvn test -Dcucumber.filter.tags="@regression"
 *
 *   Skip destructive (creates/deletes data) tests:
 *     mvn test -Dcucumber.filter.tags="not @destructive"
 *
 *   Run smoke but not negative tests:
 *     mvn test -Dcucumber.filter.tags="@smoke and not @negative"
 *
 * Available tags in this project:
 *   @smoke       — critical happy-path scenarios; fast CI gate
 *   @regression  — full regression suite
 *   @negative    — invalid input / error path scenarios
 *   @destructive — scenarios that create or delete real GitHub data
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = "cucumber.glue",   value = "stepdefs")
@ConfigurationParameter(key = "cucumber.plugin",  value =
        "pretty, json:target/cucumber-reports/cucumber.json, "
        + "html:target/cucumber-reports/cucumber.html, "
        + "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm")
@ConfigurationParameter(key = "cucumber.junit-platform.naming-strategy", value = "long")
@ConfigurationParameter(key = "cucumber.publish.quiet",                   value = "true")
// ── Tag filter — overridden at runtime via -Dcucumber.filter.tags ────────────
// Default: run everything.  CI smoke gate: -Dcucumber.filter.tags="@smoke"
@ConfigurationParameter(key = "cucumber.filter.tags",                     value = "")
// ── Parallel execution ────────────────────────────────────────────────────────
// ── Parallel execution disabled — running locally with a single machine
// GitHub.com rate-limits headless Chrome sessions; spawning >2 browsers
// simultaneously causes DevToolsActivePort crashes and request timeouts.
// Re-enable with parallelism=2 only in CI where runners have dedicated resources.
@ConfigurationParameter(key = "cucumber.execution.parallel.enabled",                  value = "false")
public class TestSuiteRunner {
}
