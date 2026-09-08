package runners;

import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * TestSuiteRunner — JUnit Platform Suite runner for Cucumber features.
 * Author: Nitheesh
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
public class TestSuiteRunner {
}
