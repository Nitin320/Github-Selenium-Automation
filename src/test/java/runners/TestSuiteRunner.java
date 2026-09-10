package runners;

<<<<<<< HEAD
=======
import org.junit.platform.suite.api.ConfigurationParameter;
>>>>>>> 8ca7cf66475326cf2e520753cbae49a56e3325e1
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
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
<<<<<<< HEAD
=======
@ConfigurationParameter(key = "cucumber.glue", value = "stepdefs")
@ConfigurationParameter(key = "cucumber.plugin", value =
        "pretty, json:target/cucumber-reports/cucumber.json, html:target/cucumber-reports/cucumber.html, io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm")
@ConfigurationParameter(key = "cucumber.junit-platform.naming-strategy", value = "long")
@ConfigurationParameter(key = "cucumber.publish.quiet", value = "true")
>>>>>>> 8ca7cf66475326cf2e520753cbae49a56e3325e1
public class TestSuiteRunner {
}
