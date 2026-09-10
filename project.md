# GitHub Selenium Automation Project Plan

**QE Training Batch - Group 5**

## Project Overview

### Target Application
- GitHub.com
- Full UI Automation

### Technology Stack

| Component | Technology |
|------------|------------|
| Language | Java |
| Build Tool | Maven |
| Automation | Selenium 4 |
| Test Framework | JUnit 5 |
| BDD Layer | Cucumber + Gherkin |
| Design Pattern | Page Object Model (POM) |
| Reporting | Extent Reports, Allure Reports |
| Test Data | Apache POI, Faker API |
| CI/CD | GitHub Actions |
| Execution | Parallel Test Execution |

---

# Team Structure & Responsibilities

## 1. Nitin K M
**Role:** Framework Architect

**Focus Area:** Core Infrastructure

### Deliverables
- Maven project structure
- `pom.xml`
- `BaseTest.java`
- `DriverManager.java`
- `DriverFactory.java`
- `BasePage.java`
- `CONTRIBUTING.md`

### Responsibilities
- Configure project folders:
  - `src/main/java`
  - `src/test/java`
  - `src/test/resources`
- Implement thread-safe WebDriver handling
- Support Chrome, Firefox, and Edge browsers
- Define coding standards
- Create reusable base classes

---

## 2. Muhammad Sulthan K M
**Role:** Framework & Reporting

**Focus Area:** Reporting engine, POM architecture, coordination

### Deliverables
- `ReportManager.java`
- `ScreenshotUtils.java`
- `ARCHITECTURE.md`
- `allure.properties`
- `extent-config.xml`

### Responsibilities
- Implement Extent Reports
- Integrate Allure reporting
- Capture screenshots as Base64
- Define and enforce POM architecture
- Produce framework architecture documentation

---

## 3. Jothi Sri S
**Role:** Tester - Authentication & Profile

**Focus Area:** Login, Logout, Profile Management

### Deliverables
- `LoginPage.java`
- `ProfilePage.java`
- `LoginTest.java`
- `LogoutTest.java`
- `ProfileTest.java`
- `login.feature`
- `LoginSteps.java`

### Responsibilities
- Automate authentication flows
- Build login-related page objects
- Implement BDD scenarios for authentication

---

## 4. M Sujin
**Role:** Tester - Repositories

**Focus Area:** Repository Lifecycle

### Deliverables
- `NewRepoPage.java`
- `RepoSettingsPage.java`
- `RepoHomePage.java`
- `RepositoryTest.java`
- `repository.feature`

### Responsibilities
- Create repository
- Update repository
- Star repository
- Fork repository
- Delete repository
- Ensure cleanup after every test

---

## 5. Muddammagari Deva Vignan
**Role:** Tester - Issues & Pull Requests

**Focus Area:** Collaboration Workflows

### Deliverables
- `IssuePage.java`
- `IssueListPage.java`
- `PullRequestPage.java`
- `IssueTest.java`
- `PullRequestTest.java`
- `issues.feature`
- `pullrequest.feature`

### Responsibilities
- Manage GitHub issues
- Labels and comments
- Pull request workflows

---

## 6. Muhammed Yazeen TM
**Role:** Tester - Search

**Focus Area:** Global Navigation & Search

### Deliverables
- `SearchPage.java`
- `ExplorePage.java`
- `SearchTest.java`
- `ExploreTest.java`
- `search.feature`
- `explore.feature`

### Responsibilities
- Search validation
- Search filters
- Language filters
- Explore/Trending sections

---

## 7. Naveen Aakash S
**Role:** Tester - Gists

**Focus Area:** GitHub Gists

### Deliverables
- `GistCreatePage.java`
- `GistViewPage.java`
- `GistListPage.java`
- `GistTest.java`
- `gist.feature`

### Responsibilities
- Create gists
- Edit gists
- Delete gists
- Validate public and secret gists

---

## 8. Neil Joe Augustine
**Role:** Tester - Code Viewer

**Focus Area:** File Navigation & Repository Browsing

### Deliverables
- `CodeBrowserPage.java`
- `FileViewPage.java`
- `CommitHistoryPage.java`
- `CodeViewerTest.java`
- `CommitHistoryTest.java`

### Responsibilities
- Folder tree navigation
- File content viewing
- Raw code viewing
- Commit history validation

---

## 9. Mohamed Arsath H
**Role:** Data Specialist

**Focus Area:** Test Data Management

### Deliverables
- `config.properties`
- `ConfigReader.java`
- `ExcelUtils.java`
- `DataProviderUtils.java`
- `FakerDataFactory.java`

### Responsibilities
- Configuration management
- Excel data handling using Apache POI
- Dynamic test data generation
- Faker-based test data support

---

## 10. Nitheesh Prakash J D
**Role:** CI/CD Execution

**Focus Area:** Pipeline Management

### Deliverables
- `.github/workflows/selenium-ci.yml`
- `junit-platform.properties`
- `TestSuiteRunner.java`

### Responsibilities
- GitHub Actions setup
- Parallel execution configuration
- Allure publishing
- Pipeline monitoring

---

# Integration Dependencies

| Member | Depends On | Provides To |
|----------|------------|-------------|
| Nitin K M | None | Entire Team |
| Muhammad Sulthan | Nitin | Entire Team |
| Mohamed Arsath | Nitin | Entire Team |
| Jothi Sri | Nitin, Arsath | Entire Team |
| M Sujin | Nitin, Jothi, Arsath | Deva, Neil |
| Deva Vignan | Nitin, Jothi, Sujin | Module Integration |
| Yazeen | Nitin, Jothi, Sujin | Module Integration |
| Naveen | Nitin, Jothi, Sujin | Module Integration |
| Neil Joe | Nitin, Jothi, Sujin | Module Integration |
| Nitheesh | All Team Members | Entire Team |

---

# Suggested Execution Plan

## Phase 1 - Foundation
### Owners
- Nitin
- Arsath
- Sulthan

### Goals
- Setup Maven project
- Create base framework
- Configure reporting
- Configure data management

---

## Phase 2 - Authentication First

### Owner
- Jothi Sri

### Goals
- Complete Login Page
- Complete Authentication Tests

**Note:** Authentication is a prerequisite for all other modules.

---

## Phase 3 - Module Development

### Owners
- Sujin
- Yazeen
- Naveen
- Neil Joe
- Deva Vignan

### Goals
- Develop module-specific page classes
- Implement automation scenarios
- Validate independent functionality

---

## Phase 4 - BDD Integration

### Owners
- All Module Owners

### Goals
- Create feature files
- Add step definitions
- Integrate with automation framework

---

## Phase 5 - CI/CD Pipeline

### Owner
- Nitheesh

### Goals
- Configure GitHub Actions
- Enable parallel execution
- Publish reports automatically

---

# Team Ground Rules

## Branching Strategy
Use feature branches:

```text
feature/nitin-core
feature/jothi-login
feature/neil-codeviewer