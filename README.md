# GitHub Selenium Automation

A BDD Selenium automation framework for GitHub UI testing, built with:
- **Selenium 4** + **WebDriverManager**
- **Cucumber 7** (BDD feature files)
- **JUnit 5** (test runner & assertions)
- **ExtentReports + Allure** (dual reporting)
- **Apache POI** (Excel test data)
- **Java Faker** (random data generation)

---

## Project Structure

```
github-selenium-automation/
├── .github/workflows/
│   └── selenium-ci.yml                        ← Nitheesh
├── src/main/java/
│   ├── driver/
│   │   ├── DriverManager.java                 ← Nitin
│   │   └── DriverFactory.java                 ← Nitin
│   ├── pages/
│   │   ├── BasePage.java                      ← Nitin
│   │   ├── LoginPage.java                     ← Jothi Sri
│   │   ├── ProfilePage.java                   ← Jothi Sri
│   │   ├── NewRepoPage.java                   ← Sujin
│   │   ├── RepoHomePage.java                  ← Sujin
│   │   ├── IssuePage.java                     ← Deva Vignan
│   │   ├── PullRequestPage.java               ← Deva Vignan
│   │   ├── SearchPage.java                    ← Yazeen
│   │   ├── ExplorePage.java                   ← Yazeen
│   │   ├── GistCreatePage.java                ← Naveen
│   │   ├── GistViewPage.java                  ← Naveen
│   │   ├── CodeBrowserPage.java               ← Neil Joe
│   │   ├── FileViewPage.java                  ← Neil Joe
│   │   └── CommitHistoryPage.java             ← Neil Joe
│   ├── reporting/
│   │   └── ReportManager.java                 ← Sulthan
│   └── utils/
│       ├── ConfigReader.java                  ← Arsath
│       ├── ExcelUtils.java                    ← Arsath
│       ├── FakerDataFactory.java              ← Arsath
│       └── ScreenshotUtils.java               ← Sulthan
├── src/test/java/
│   ├── base/
│   │   └── BaseTest.java                      ← Nitin
│   ├── runners/
│   │   └── TestSuiteRunner.java               ← Nitheesh
│   ├── stepdefs/
│   │   ├── LoginSteps.java                    ← Jothi Sri
│   │   ├── RepositorySteps.java               ← Sujin
│   │   ├── IssueSteps.java                    ← Deva Vignan
│   │   ├── SearchSteps.java                   ← Yazeen
│   │   ├── GistSteps.java                     ← Naveen
│   │   └── CodeViewerSteps.java               ← Neil Joe
│   └── tests/
│       ├── LoginTest.java                     ← Jothi Sri
│       ├── RepositoryTest.java                ← Sujin
│       ├── IssueTest.java                     ← Deva Vignan
│       ├── SearchTest.java                    ← Yazeen
│       ├── GistTest.java                      ← Naveen
│       └── CodeViewerTest.java                ← Neil Joe
└── src/test/resources/
    ├── config.properties                      ← Arsath
    ├── extent-config.xml                      ← Sulthan
    ├── allure.properties                      ← Sulthan
    ├── junit-platform.properties              ← Nitheesh
    ├── features/
    │   ├── login.feature                      ← Jothi Sri
    │   ├── repository.feature                 ← Sujin
    │   ├── issues.feature                     ← Deva Vignan
    │   ├── search.feature                     ← Yazeen
    │   ├── gist.feature                       ← Naveen
    │   └── codeviewer.feature                 ← Neil Joe
    └── testdata/
        └── github_testdata.xlsx               ← Arsath
```

---

## Setup

### Prerequisites
- Java 17+
- Maven 3.9+
- Chrome/Firefox/Edge installed

### Configuration

Copy the template and supply your credentials via environment variables — **never commit credentials**:

```bash
export GITHUB_USERNAME=your_github_username
export GITHUB_PASSWORD=your_github_password
```

The [`config.properties`](src/test/resources/config.properties) file reads these via `${GITHUB_USERNAME}` placeholders.

---

## Running Tests

```bash
# Run all tests (headless Chrome)
mvn test -Dheadless=true

# Run with a specific browser
mvn test -Dbrowser=firefox -Dheadless=true

# Run in headed mode locally
mvn test -Dheadless=false
```

---

## Reports

| Report | Location |
|--------|----------|
| Cucumber HTML | `target/cucumber-reports/cucumber.html` |
| ExtentReports | `target/extent-reports/ExtentReport_*.html` |
| Allure | `mvn allure:serve` |
| Screenshots | `target/screenshots/` |

---

## Team

| File(s) | Owner |
|---------|-------|
| `selenium-ci.yml`, `TestSuiteRunner`, `junit-platform.properties` | Nitheesh |
| `DriverManager`, `DriverFactory`, `BasePage`, `BaseTest` | Nitin |
| `LoginPage`, `ProfilePage`, `LoginSteps`, `LoginTest`, `login.feature` | Jothi Sri |
| `NewRepoPage`, `RepoHomePage`, `RepositorySteps`, `RepositoryTest`, `repository.feature` | Sujin |
| `IssuePage`, `PullRequestPage`, `IssueSteps`, `IssueTest`, `issues.feature` | Deva Vignan |
| `SearchPage`, `ExplorePage`, `SearchSteps`, `SearchTest`, `search.feature` | Yazeen |
| `GistCreatePage`, `GistViewPage`, `GistSteps`, `GistTest`, `gist.feature` | Naveen |
| `CodeBrowserPage`, `FileViewPage`, `CommitHistoryPage`, `CodeViewerSteps`, `CodeViewerTest`, `codeviewer.feature` | Neil Joe |
| `ReportManager`, `ScreenshotUtils`, `extent-config.xml`, `allure.properties` | Sulthan |
| `ConfigReader`, `ExcelUtils`, `FakerDataFactory`, `config.properties`, `github_testdata.xlsx` | Arsath |
