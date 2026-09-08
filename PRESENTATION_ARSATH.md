```markdown
# Data & Configuration Management — Presentation & Contribution Overview

**Role:** Data Specialist (Test Data & Configuration Management)  
**Author:** Mohamed Arsath H  
**Project:** GitHub Selenium Automation Framework  

---

## 1. System Architecture & Data Flow

```text
 ┌─────────────────────────────────────────────────────────┐
 │                       CONFIGURATION                     │
 │  config.properties ──► ConfigReader.java (${ENV_VAR})   │
 └────────────────────────────┬────────────────────────────┘
                              │
               ┌──────────────┴──────────────┐
               ▼                             ▼
    ┌────────────────────┐        ┌────────────────────┐
    │    STATIC DATA     │        │    DYNAMIC DATA    │
    │  github_testdata   │        │  FakerDataFactory  │
    │    ExcelUtils      │        │                    │
    │ DataProviderUtils  │        │                    │
    └──────────┬─────────┘        └──────────┬─────────┘
               │                             │
               └──────────────┬──────────────┘
                              │
                              ▼
    ┌──────────────────────────────────────────────────┐
    │                STEP DEFINITIONS                  │
    │  LoginSteps (Jothi Sri)   RepositorySteps (Sujin)│
    │  IssueSteps (Deva)        GistSteps (Naveen)     │
    │  SearchSteps (Yazeen)     CodeViewerSteps (Neil) │
    └──────────────────────────────────────────────────┘

```

---

## 2. Summary of Deliverables & Core Responsibilities

* **Environment Infrastructure:** Created `config.properties` and built `ConfigReader.java` with multi-tier property resolution (`System.getenv` $\rightarrow$ local `.env` $\rightarrow$ defaults).


* **Static Test Data Pipeline:** Built `ExcelUtils.java` using Apache POI and enhanced it with `DataProviderUtils.java` to allow key-value (`Map<String, String>`) access, decoupling tests from rigid column ordering.


* **Dynamic Data Generation:** Implemented `FakerDataFactory.java` using Java Faker to generate randomized repository names, issue titles, and gists, protecting parallel runs against GitHub entity collision.


* **Unified Data Repository:** Created and managed the 5-sheet `github_testdata.xlsx` workbook (supported by a zero-dependency setup generator script `GenerateTestData.java`) to establish data contracts across all execution modules.


* **Security & Compliance:** Enforced strict secret handling by leveraging `${GITHUB_USERNAME}` and `${GITHUB_PASSWORD}` placeholders, keeping sensitive credentials entirely out of target properties and spreadsheets.



---

## 3. Respective Component Explanations

### Configuration Management (`config.properties` & `ConfigReader.java`)

* **Purpose:** Provides a centralized, secure configuration loader to prevent hardcoded URLs, timeouts, and credentials across step definitions.


* **Mechanism:** Reads property key-value pairs at runtime and automatically resolves environment variable placeholders like `${GITHUB_USERNAME}` via `System.getenv()` or local `.env` files.



### Static Test Data Engine (`ExcelUtils.java` & `github_testdata.xlsx`)

* **Purpose:** Enables data-driven testing using human-editable spreadsheets so team members can manage test scenarios without altering Java code.


* **Mechanism:** Leverages Apache POI to parse sheets (`LoginData`, `RepoData`, `IssueData`, `GistData`, `SearchData`), converting cell types safely into standard strings.



### Test Data Abstraction (`DataProviderUtils.java`)

* **Purpose:** Protects step definitions from breaking when spreadsheet columns are added or reordered.


* **Mechanism:** Wraps `ExcelUtils` and converts row arrays into key-value maps (`Map<String, String>`), enabling column access by header name rather than fixed numeric array indices.



### Dynamic Test Data Generation (`FakerDataFactory.java`)

* **Purpose:** Prevents entity collision errors on GitHub (e.g., duplicate repository or Gist names) during parallel or automated execution runs.


* **Mechanism:** Utilizes Java Faker to synthesize random, unique strings at execution time to keep test runs isolated and reproducible.



### Data Setup Generator (`GenerateTestData.java`)

* **Purpose:** Guarantees workbook format integrity without requiring manual Excel installation or external spreadsheet software.


* **Mechanism:** A standalone Java script that programmatically builds and refreshes `github_testdata.xlsx` directly from baseline source constants.



```

```