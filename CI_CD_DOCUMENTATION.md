# CI/CD Documentation
## GitHub Selenium Automation — Group 5, IBM QE Training

**Author:** Nitheesh Prakash  
**Role:** CI/CD Execution  
**Last Updated:** September 2026

---

## Table of Contents

1. [What Is CI/CD in This Project?](#1-what-is-cicd-in-this-project)
2. [How Our Selenium Tests Work](#2-how-our-selenium-tests-work)
3. [GitHub Actions Workflow](#3-github-actions-workflow)
4. [Parallel Execution](#4-parallel-execution)
5. [JUnit 5 Configuration](#5-junit-5-configuration)
6. [Maven Execution](#6-maven-execution)
7. [GitHub Secrets — Credentials](#7-github-secrets--credentials)
8. [Reports and Screenshots](#8-reports-and-screenshots)
9. [How the Pipeline Is Triggered](#9-how-the-pipeline-is-triggered)
10. [Where to See Failures, Logs and Reports](#10-where-to-see-failures-logs-and-reports)
11. [How to Run Tests Locally](#11-how-to-run-tests-locally)
12. [Troubleshooting and Common CI Failures](#12-troubleshooting-and-common-ci-failures)
13. [Changes Made to Other Team Members' Files](#13-changes-made-to-other-team-members-files)
14. [Files Created / Modified by Nitheesh](#14-files-created--modified-by-nitheesh)
15. [Manual Checklist for Nitheesh](#15-manual-checklist-for-nitheesh)

---

## 1. What Is CI/CD in This Project?

**CI/CD** stands for **Continuous Integration / Continuous Delivery**.

- **Continuous Integration (CI):** Every time someone pushes code or opens a pull request, our test suite runs automatically on GitHub's servers. If the tests break, the team is notified immediately — before the bad code reaches the main branch.
- **Continuous Delivery (CD):** In this project the "delivery" is the test reports: after every run, the pipeline packages up our HTML report, Allure results, screenshots, and logs as downloadable artifacts so the team can inspect exactly what happened.

**Why it matters for a Selenium framework:**  
Selenium tests require a real (or headless) browser. Setting all of that up by hand every time is slow and error-prone. GitHub Actions does it automatically on a clean Linux machine every single run, which means:
- You always test against the same environment.
- Flaky environment differences ("it worked on my laptop!") are eliminated.
- Failures are caught within minutes of a push, not days later.

---

## 2. How Our Selenium Tests Work

```
GitHub.com (live website)
        ↑  HTTP/WebDriver
  ChromeDriver (headless)
        ↑
   WebDriver (Selenium 4)
        ↑
  Page Object Model (pages/ package)
        ↑
 Step Definitions (stepdefs/ package)  ←── Cucumber .feature files
        ↑                                        ↑
  JUnit 5 test classes (tests/ package)   BDD scenarios
        ↑
   BaseTest.java (sets up / tears down WebDriver)
        ↑
  DriverManager.java (ThreadLocal<WebDriver> — thread-safe for parallel execution)
        ↑
   DriverFactory.java (creates ChromeDriver with correct headless options)
        ↑
  ConfigReader.java (reads config.properties; system properties override)
```

Key points:
- **We test the real GitHub.com** — not a mock or staging server.
- **`ThreadLocal<WebDriver>`** means each parallel thread gets its own isolated browser instance, so tests never interfere with each other.
- **`-Dheadless=true`** tells ChromeDriver to run without opening a visible window (required in CI where there is no display).
- **Credentials** (GitHub username and password) come from environment variables, never from the code or `config.properties`.

---

## 3. GitHub Actions Workflow

**File:** [`.github/workflows/selenium-ci.yml`](.github/workflows/selenium-ci.yml)

### Workflow overview

```
Trigger (push / PR / manual)
    │
    ▼
ubuntu-latest runner
    │
    ├─ Step 1: Checkout code
    ├─ Step 2: Set up Java 17 (Temurin)
    ├─ Step 3: Cache ~/.m2 (faster subsequent runs)
    ├─ Step 4: Install Google Chrome
    ├─ Step 5: Run mvn test -Dheadless=true  ← main step
    ├─ Step 6: Print test summary to log
    ├─ Step 7: Upload Cucumber HTML report   ┐
    ├─ Step 8: Upload ExtentReports          │ always run,
    ├─ Step 9: Upload Allure results         │ even on failure
    ├─ Step 10: Upload screenshots           │
    └─ Step 11: Upload Surefire logs         ┘
```

### Step-by-step explanation

| Step | What it does | Why it's needed |
|------|-------------|-----------------|
| Checkout | Downloads the repository code onto the runner | Nothing works without the source |
| Java 17 | Installs the JDK | Our code targets Java 17 |
| Maven cache | Saves ~/.m2 between runs | Cuts 2–3 minutes off every run |
| Chrome | Installs headless Chrome | Selenium needs a real Chrome binary |
| Run tests | `mvn test -Dheadless=true` | Executes the Cucumber suite |
| Summary | Parses cucumber.json, prints pass/fail counts | Makes failures visible at a glance |
| Artifacts | Uploads reports + screenshots | Team can download and inspect results |

### Credentials in the workflow

```yaml
env:
  GITHUB_USERNAME: ${{ secrets.GITHUB_USERNAME }}
  GITHUB_PASSWORD: ${{ secrets.GITHUB_PASSWORD }}
```

These are **GitHub Secrets** — encrypted values stored in the repository settings, never visible in logs or code. See [Section 7](#7-github-secrets--credentials) for how to set them up.

---

## 4. Parallel Execution

### Why parallel?

Running 4+ Cucumber scenarios sequentially takes 2–3× longer. Parallel execution runs multiple scenarios at the same time on different threads, cutting total time significantly.

### How it's implemented — two layers

| Layer | Config file | Setting | What runs in parallel |
|-------|------------|---------|----------------------|
| **Cucumber scenarios** | `TestSuiteRunner.java` | `fixed parallelism = 2` | Scenarios from all feature files run on 2 threads simultaneously |
| **Plain JUnit 5 tests** | `junit-platform.properties` | `classes.default = concurrent, parallelism = 2` | LoginTest, ProfileTest, LogoutTest, etc. run in parallel when invoked directly |

### Thread safety — why it's safe

The critical piece is [`DriverManager.java`](src/main/java/driver/DriverManager.java):

```java
private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
```

`ThreadLocal` means each thread has its **own copy** of the WebDriver variable. Thread A's browser is completely separate from Thread B's browser. They can never see or interfere with each other's browser session.

The `@Before` hook in `LoginSteps` and the `@BeforeEach` in `BaseTest` both call `DriverManager.setDriver(DriverFactory.createDriver())`, which sets a brand-new driver on the current thread before each test. The `@After` / `@AfterEach` calls `DriverManager.quitDriver()`, which quits the browser and removes it from the ThreadLocal.

### Parallelism setting: 2 threads

Two threads is a conservative, safe default. It means:
- CI runner memory stays well within the 7 GB available on `ubuntu-latest`.
- Two Chrome instances run at the same time.
- When all teams have filled in their feature files and page objects, increase to 4 by changing the `fixed.parallelism` values in both files.

---

## 5. JUnit 5 Configuration

**File:** [`src/test/resources/junit-platform.properties`](src/test/resources/junit-platform.properties)

```properties
# Parallel execution for plain @Test classes
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=same_thread
junit.jupiter.execution.parallel.mode.classes.default=concurrent
junit.jupiter.execution.parallel.config.strategy=fixed
junit.jupiter.execution.parallel.config.fixed.parallelism=2
```

**What each line means:**

| Property | Value | Meaning |
|----------|-------|---------|
| `parallel.enabled` | `true` | Switch on parallel execution |
| `mode.default` | `same_thread` | Methods inside one test class stay on one thread (so `@BeforeEach` and the test method always see the same WebDriver) |
| `mode.classes.default` | `concurrent` | Different test classes run at the same time |
| `config.strategy` | `fixed` | Use a fixed number of threads |
| `config.fixed.parallelism` | `2` | Two threads |

**Important:** Cucumber-specific settings (`cucumber.features`, `cucumber.glue`, `cucumber.plugin`, `cucumber.execution.parallel.*`) do **NOT** live in this file. They live in `TestSuiteRunner.java`. This is intentional — if Cucumber config were in `junit-platform.properties`, the Cucumber engine would activate for every JUnit run and hijack individual test class executions.

---

## 6. Maven Execution

### How Maven runs the tests

1. Maven reads `pom.xml`.
2. The `maven-surefire-plugin` (version 3.2.5) is configured to include only `**/TestSuiteRunner.java`.
3. The JUnit Platform Suite Engine reads `TestSuiteRunner`'s `@Suite` + `@SelectClasspathResource("features")` annotations and discovers all `.feature` files.
4. The Cucumber JUnit Platform Engine runs all scenarios in those feature files using the step definitions in the `stepdefs` package.

### Key Maven command

```bash
# Runs everything in headless mode (the standard CI command)
mvn clean test -Dheadless=true

# Run with a specific browser
mvn clean test -Dheadless=true -Dbrowser=chrome

# Run only a specific plain JUnit test class
mvn test -Dtest=LoginTest -Dheadless=true

# Run multiple plain JUnit test classes in parallel
mvn test -Dtest="LoginTest,ProfileTest,LogoutTest" -Dheadless=true
```

### How `-Dheadless=true` reaches the test code

```
mvn test -Dheadless=true
    │
    ├── pom.xml Surefire <systemPropertyVariables>
    │       <headless>${headless}</headless>     ← forwards the -D value
    │
    ▼
ConfigReader.getProperty("headless", "false")
    │
    ├── System.getProperty("headless")  ← checks JVM system properties FIRST
    │       returns "true"
    ▼
DriverFactory.createDriver()
    │
    ├── boolean headless = Boolean.parseBoolean("true")   → true
    ▼
ChromeOptions.addArguments("--headless=new")   → headless Chrome!
```

### pom.xml highlights

| Addition | Why |
|----------|-----|
| `maven.compiler.release=17` | Replaces deprecated `-source/-target` flags; eliminates compiler warnings |
| `junit-platform-launcher` dependency | Required by Surefire 3.x to run JUnit Platform suites |
| `junit-platform-suite-engine` dependency | Executes `@Suite` annotated classes |
| `<systemPropertyVariables>` in Surefire | Passes `-Dheadless` and `-Dbrowser` to the forked JVM |

---

## 7. GitHub Secrets — Credentials

### What are GitHub Secrets?

GitHub Secrets are encrypted variables stored in the repository (or organization) settings. They are:
- **Never visible in logs** — GitHub masks them automatically.
- **Never in source code** — they exist only in the encrypted secrets store.
- **Injected as environment variables** during workflow runs.

### The two secrets this project needs

| Secret name | What it holds | Example |
|-------------|--------------|---------|
| `GITHUB_USERNAME` | The GitHub account username used for test login | `mytestacc` |
| `GITHUB_PASSWORD` | The GitHub account password | `MyS3cureP@ss!` |

### How to set them up (one-time setup, done by Nitheesh)

1. Go to your repository on GitHub.com.
2. Click **Settings** → **Secrets and variables** → **Actions**.
3. Click **New repository secret**.
4. Name: `GITHUB_USERNAME`, Value: the test account username.
5. Click **Add secret**.
6. Repeat for `GITHUB_PASSWORD`.

> ⚠️ **Important:** Use a **dedicated test account**, not your personal GitHub account. This account will be repeatedly logged in and out during CI runs.

### How they flow from GitHub → test code

```
GitHub Secrets Store
    │
    ▼ (workflow step)
env:
  GITHUB_USERNAME: ${{ secrets.GITHUB_USERNAME }}
  GITHUB_PASSWORD: ${{ secrets.GITHUB_PASSWORD }}
    │
    ▼ (OS environment variable inside the runner)
ConfigReader.resolveEnvPlaceholder("${GITHUB_USERNAME}")
    → System.getenv("GITHUB_USERNAME")
    → returns the actual username
    │
    ▼
LoginPage.loginFromConfig() → uses the username and password
```

### The `config.properties` placeholder

```properties
# src/test/resources/config.properties
github.username=${GITHUB_USERNAME}
github.password=${GITHUB_PASSWORD}
```

The `${...}` syntax is **not a shell variable** — it is a custom placeholder that `ConfigReader` resolves at runtime by calling `System.getenv()`. The actual credentials never touch this file.

---

## 8. Reports and Screenshots

### Where reports are written

| Report | Location on disk | Uploaded artifact name |
|--------|-----------------|----------------------|
| Cucumber HTML | `target/cucumber-reports/cucumber.html` | `cucumber-report-<run#>` |
| Cucumber JSON | `target/cucumber-reports/cucumber.json` | same artifact |
| ExtentReports | `target/extent-reports/ExtentReport_<timestamp>.html` | `extent-report-<run#>` |
| Allure results | `target/allure-results/` | `allure-results-<run#>` |
| Screenshots | `target/screenshots/*.png` | `screenshots-<run#>` |
| Surefire XML | `target/surefire-reports/` | `surefire-reports-<run#>` |

### When screenshots are taken

Screenshots are captured automatically in `BaseTest.tearDown()` after every test method (pass or fail). The file name is sanitized from the test name. For Cucumber scenarios, hooks in `LoginSteps.tearDown()` call `DriverManager.quitDriver()`.

To add screenshots to a specific step, call:
```java
ScreenshotUtils.capture("my-step-name");
```

### How to generate the Allure report locally

```bash
# After running tests:
mvn allure:report

# Then open: target/site/allure-maven-plugin/index.html
```

### Artifact retention

All artifacts are kept for **30 days**. After that, GitHub automatically deletes them to save storage.

---

## 9. How the Pipeline Is Triggered

The workflow (`.github/workflows/selenium-ci.yml`) runs on three events:

| Trigger | When | Example |
|---------|------|---------|
| **Push to master** | Every commit pushed to `master` | A teammate merges their feature branch |
| **Pull Request to master** | Every PR opened or updated targeting `master` | A teammate opens a PR for review |
| **Manual (`workflow_dispatch`)** | Manually from the GitHub Actions tab | You want to re-run tests without pushing code |

### How to trigger a manual run

1. Go to your repository → **Actions** tab.
2. Click **Selenium CI** in the left sidebar.
3. Click **Run workflow** (top right).
4. Choose the branch and optionally change `headless` to `false` for debugging.
5. Click **Run workflow**.

---

## 10. Where to See Failures, Logs and Reports

### Seeing test results on GitHub

1. Go to your repository → **Actions** tab.
2. Click on the latest workflow run.
3. The job `Run Selenium BDD Tests` shows green (pass) or red (fail).
4. Click on the job to expand the steps.
5. Expand **Run Selenium BDD Tests (headless)** to see the Maven console output.
6. Expand **Print test summary** to see a quick pass/fail count.
7. Scroll to the bottom of the page — the **Artifacts** section has download links for reports and screenshots.

### Reading the Cucumber report

Download `cucumber-report-<run#>` and open `cucumber.html` in any browser. It shows:
- Which feature file and scenario passed/failed.
- The exact step that failed and the exception message.
- Step-level timing information.

### Reading the ExtentReport

Download `extent-report-<run#>` and open `ExtentReport_<timestamp>.html`. It shows:
- A visual dashboard with pass/fail percentages.
- Detailed logs for each test.
- System information (browser, headless mode, environment).

### Finding a screenshot of a failure

Download `screenshots-<run#>` and look for the `.png` file named after the failing test.

---

## 11. How to Run Tests Locally

### Prerequisites

- Java 17 or later installed
- Maven 3.6+ installed
- Google Chrome installed (any recent version — WebDriverManager downloads the matching ChromeDriver automatically)
- A `.env` file in the project root **OR** environment variables set:

```bash
# Option A: .env file (create this file, it is already in .gitignore)
GITHUB_USERNAME=your_test_account_username
GITHUB_PASSWORD=your_test_account_password
```

```powershell
# Option B: PowerShell environment variables (current session only)
$env:GITHUB_USERNAME="your_test_account_username"
$env:GITHUB_PASSWORD="your_test_account_password"
```

### Running the full Cucumber BDD suite

```bash
# Headless Chrome (fastest, same as CI)
mvn clean test -Dheadless=true

# Visible Chrome (useful for debugging)
mvn clean test
```

### Running a specific feature file

Maven Surefire does not support filtering by feature file directly, but you can add a Cucumber tag to the feature and pass it:

```bash
mvn test -Dheadless=true -Dcucumber.filter.tags="@login"
```

### Running plain JUnit 5 test classes

```bash
# Single class
mvn test -Dtest=LoginTest -Dheadless=true

# Multiple classes
mvn test -Dtest="LoginTest,ProfileTest,LogoutTest" -Dheadless=true
```

### Opening reports after a local run

- **Cucumber HTML:** `target/cucumber-reports/cucumber.html`
- **ExtentReports:** `target/extent-reports/ExtentReport_<timestamp>.html`
- **Allure:** `mvn allure:report` then open `target/site/allure-maven-plugin/index.html`

---

## 12. Troubleshooting and Common CI Failures

### "Tests run: 0" — no tests executed

**Cause:** Maven couldn't find the test class.  
**Fix:** Ensure the Surefire include is `**/TestSuiteRunner.java` in `pom.xml`. Also verify `TestSuiteRunner.class` exists in `target/test-classes/runners/`.

---

### "TimeoutException: Expected condition failed" on login steps

**Cause 1:** `GITHUB_USERNAME` or `GITHUB_PASSWORD` secrets are not set (most common in CI).  
**Fix:** Add the secrets in GitHub repository Settings → Secrets and variables → Actions.

**Cause 2:** GitHub.com UI changed and the CSS selector for the account menu button no longer matches.  
**Fix:** Open `LoginPage.java` and update `accountMenuButton` selector. GitHub's button has `data-login="<username>"` attribute — this is reliable.

---

### "SessionNotCreatedException" / "ChromeDriver not found"

**Cause:** WebDriverManager could not download ChromeDriver (network issue in CI, or version mismatch).  
**Fix:** The `Install Chrome` step in the workflow ensures Chrome is present. WebDriverManager detects the installed Chrome version and downloads the matching ChromeDriver automatically. If the error persists, check the runner's outbound internet access.

---

### "Could not load config.properties"

**Cause:** The file is not on the classpath.  
**Fix:** Ensure `config.properties` is in `src/test/resources/` and Maven's `testResources` section copies it to `target/test-classes/`.

---

### Build fails but no feature file errors — just "BUILD FAILURE"

**Cause:** A previous Surefire run left a corrupted dump file.  
**Fix:** Run `mvn clean test -Dheadless=true` (the `clean` phase deletes `target/`).

---

### Parallel test interference — one test's driver sees another's page

**Cause:** Someone stored `WebDriver` in a plain `static` field instead of through `DriverManager`.  
**Fix:** All WebDriver access must go through `DriverManager.getDriver()`. Never use a `static WebDriver` field in a page object or step definition.

---

### GitHub reports "Resource not accessible by integration" for GITHUB_TOKEN

**Cause:** The workflow uses `secrets.GITHUB_TOKEN` (the default Actions token) but needs write permissions.  
**Note:** This project uses `secrets.GITHUB_USERNAME` and `secrets.GITHUB_PASSWORD` for **logging into GitHub.com via Selenium** — not for GitHub API calls. These are separate things. The Actions `GITHUB_TOKEN` is not involved here.

---

### `workflow_dispatch` not showing in the Actions tab

**Cause:** The `workflow_dispatch` trigger was not present in the workflow file when the branch was last pushed.  
**Fix:** Ensure the trigger block is on the default branch (master). GitHub only shows the manual-trigger UI for workflow files on the default branch.

---

## 13. Changes Made to Other Team Members' Files

This section documents every file that belongs to a teammate that was modified as part of CI/CD integration, the reason for the change, and whether it affects their tests.

---

### `src/test/java/base/BaseTest.java`
**Owner:** Nitin (Framework / Driver setup)  
**Original purpose:** JUnit 5 base class that sets up and tears down WebDriver for all test classes.

**What was changed:**
```java
// BEFORE (line 34):
ReportManager.flush();

// AFTER:
ReportManager.flushReports();
```

**Why it was necessary:**  
`ReportManager` (authored by Sulthan) exposes the method as `flushReports()`, not `flush()`. The old call caused a **compilation error** that prevented the entire project from building. The fix is a one-word rename to match the actual method signature.

**Impact on teammate's tests:**  
Zero functional change. The tearDown behaviour is identical — it still flushes the ExtentReport after every test. Only the method name was corrected.

---

### `src/main/java/utils/ConfigReader.java`
**Owner:** Arsath (Configuration / Test Data)  
**Original purpose:** Reads `config.properties` from the classpath, resolves `${VAR}` placeholders against environment variables.

**What was changed:**  
Added a system-property check at the top of both `getProperty` overloads, before the properties file lookup:

```java
// NEW — added as the first check in getProperty():
String sysProp = System.getProperty(key);
if (sysProp != null && !sysProp.isEmpty()) {
    return sysProp;
}
```

**Why it was necessary:**  
The Maven command `mvn test -Dheadless=true` sets a **Java system property** (via `-D`). `DriverFactory` reads `ConfigReader.get("headless", "false")`, which previously only looked at `config.properties`. The file has `headless=false`, so the `-Dheadless=true` flag on the command line was silently ignored — Chrome launched in headed mode even when CI requested headless.

The new resolution order is:
1. Java system property (`-Dheadless=true` from Maven)
2. `config.properties` on the classpath
3. Environment variable placeholder (`${VAR}`)
4. Default value

**Impact on teammate's tests:**  
The change adds a higher-priority lookup but does **not remove** any existing lookup. All existing tests that relied on `config.properties` or environment variables continue to work unchanged. The only new behaviour is that `-D` flags from the Maven command line now take effect.

---

### `src/test/java/pages/LoginPageTest.java`
**Owner:** (Authors unknown — class exists in `src/test/java/pages/` package)  
**Original purpose:** A quick smoke test that the `LoginPage` page object can open `github.com`.

**What was changed:**  
Added a missing `@AfterEach tearDown()` method and a Javadoc comment:

```java
// ADDED:
@AfterEach
void tearDown() {
    DriverManager.quitDriver();
}
```

Also added the `import org.junit.jupiter.api.AfterEach;` statement.

**Why it was necessary:**  
The original class had `@BeforeEach` that created a `ChromeDriver` but no matching teardown. In local runs this leaks browser processes. In CI on `ubuntu-latest` it causes orphaned Chrome processes that consume all available memory and crash the runner — it would silently corrupt any parallel run.

**Impact on teammate's tests:**  
Zero functional change to the test logic. The test still opens GitHub, performs a login, and asserts the URL. The only addition is that the browser is properly closed after the test completes.

---

## 14. Files Created / Modified by Nitheesh

| File | Action | Summary |
|------|--------|---------|
| `.github/workflows/selenium-ci.yml` | **Completed** | Added `workflow_dispatch` trigger, Chrome install step, `Xvfb` virtual display, `--no-transfer-progress`, test summary step, Allure artifact upload, Surefire log upload, run-number suffixes on artifact names, `timeout-minutes`, `if-no-files-found: warn`, 30-day retention |
| `src/test/resources/junit-platform.properties` | **Rewritten** | Expanded comments explaining the design decision (no Cucumber config here), confirmed the 5 parallel-execution properties |
| `src/test/java/runners/TestSuiteRunner.java` | **No change needed** | Already correct — all Cucumber `@ConfigurationParameter` settings were properly scoped here |
| `pom.xml` | **Modified** | Changed `maven.compiler.source/target → release=17` (removes warnings); added `junit-platform-launcher` and `junit-platform-suite-engine` dependencies; added `<systemPropertyVariables>` block in Surefire to forward `-Dheadless` and `-Dbrowser`; added Allure Maven plugin `resultsDirectory` config |
| `CI_CD_DOCUMENTATION.md` | **Created** | This document |

---

## 15. Manual Checklist for Nitheesh

These are the only things that **cannot be done automatically** and require manual action by you:

- [ ] **Set GitHub Secrets** — Go to the repository on GitHub → Settings → Secrets and variables → Actions → add `GITHUB_USERNAME` and `GITHUB_PASSWORD` with the test account credentials. The pipeline will fail with `TimeoutException` (login timeout) until these are set.

- [ ] **Verify the test account** — The account stored in the secrets must:
  - Not have two-factor authentication (2FA) enabled, OR have a method to bypass it for automation.
  - Not be your personal account.
  - Be able to log in at `https://github.com/login` with username + password.

- [ ] **Push this branch to GitHub** — The completed workflow only takes effect once it is merged/pushed to `master`. Push the changes, open the Actions tab, and confirm the workflow appears.

- [ ] **Confirm the first pipeline run** — After pushing, go to Actions → Selenium CI → check that all 11 steps complete. The `Run Selenium BDD Tests` step should show 4 scenarios: 2 pass (invalid/empty password), 2 pass (valid login and logout) once credentials are set.

- [ ] **Optional — increase parallelism when teammates finish their work** — Once all feature files have scenarios and all step definitions are implemented, change `fixed.parallelism` from `2` to `4` in both:
  - `TestSuiteRunner.java` (`cucumber.execution.parallel.config.fixed.parallelism`)
  - `junit-platform.properties` (`junit.jupiter.execution.parallel.config.fixed.parallelism`)

---

*Document prepared by Nitheesh Prakash — CI/CD Execution, Group 5, IBM QE Training*
