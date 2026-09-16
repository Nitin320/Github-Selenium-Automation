# GitHub Issues Feature
# Author: Deva Vignan

@regression
Feature: GitHub Issues

  Background:
    Given the user is logged into GitHub
    And a GitHub repository is configured

  # ══════════════════════════════════════════════════════════════════════════
  #  SMOKE — happy path (runs on every push)
  # ══════════════════════════════════════════════════════════════════════════

  @smoke
  Scenario: Open the repository issues page
    When I open the repository issues page
    Then the issues page should be displayed

  @destructive
  Scenario: Create a new issue
    When I open the repository issues page
    And I create an issue with title "Automated issue - selenium test"
    And I enter the issue description "Issue created by Selenium automation for QA demonstration"
    And I submit the issue
    Then the issue should be created successfully

  # ══════════════════════════════════════════════════════════════════════════
  #  DATA-DRIVEN — create issues with different titles
  # ══════════════════════════════════════════════════════════════════════════

  @destructive
  Scenario Outline: Create issues with different titles and descriptions
    When I open the repository issues page
    And I create an issue with title "<title>"
    And I enter the issue description "<description>"
    And I submit the issue
    Then the issue should be created successfully

    Examples:
      | title                                  | description                                 |
      | [Bug] Button not responding on mobile  | Reproducible on iOS Safari, steps attached  |
      | [Feature] Dark mode toggle             | Users frequently request dark mode support  |
      | [Docs] Update README with setup steps  | README is outdated since last major release |

  # ══════════════════════════════════════════════════════════════════════════
  #  NEGATIVE — validation and error paths
  # ══════════════════════════════════════════════════════════════════════════

  @negative
  Scenario: Issue creation is blocked when title is empty
    When I open the repository issues page
    And I attempt to submit a new issue with an empty title
    Then the issue form should indicate a title is required

  @negative
  Scenario: Issue title with only whitespace is rejected
    When I open the repository issues page
    And I attempt to submit a new issue with title "   "
    Then the issue form should indicate a title is required

  @negative
  Scenario: Issues page shows correct empty state when no issues exist
    When I open the repository issues page
    Then the issues page should be displayed
    And the page should show either a list of issues or an empty state message
