# GitHub Search Feature
# Author: Yazeen

@regression
Feature: GitHub Search and Explore

  # ══════════════════════════════════════════════════════════════════════════
  #  SMOKE — happy path (runs on every push)
  # ══════════════════════════════════════════════════════════════════════════

  @smoke
  Scenario: Search GitHub repositories
    Given I am on the GitHub search page
    When I search for "selenium"
    Then the GitHub search results page is displayed
    And the search results contain "selenium"

  @smoke
  Scenario: Explore trending repositories
    Given I am on the GitHub Explore page
    Then the GitHub Explore page is displayed
    When I open trending repositories
    Then the GitHub trending page is displayed

  # ══════════════════════════════════════════════════════════════════════════
  #  DATA-DRIVEN — search for multiple well-known terms
  # ══════════════════════════════════════════════════════════════════════════

  @regression
  Scenario Outline: Search returns results for known programming topics
    Given I am on the GitHub search page
    When I search for "<query>"
    Then the GitHub search results page is displayed
    And the search results contain "<query>"

    Examples:
      | query      |
      | selenium   |
      | junit5     |
      | cucumber   |
      | spring     |

  # ══════════════════════════════════════════════════════════════════════════
  #  NEGATIVE — edge cases in search
  # ══════════════════════════════════════════════════════════════════════════

  @negative
  Scenario: Search with special characters does not crash the page
    Given I am on the GitHub search page
    When I search for "<script>alert(1)</script>"
    Then the GitHub search results page is displayed
    And the page should not contain a script injection alert

  @negative
  Scenario: Search with a very long query string is handled gracefully
    Given I am on the GitHub search page
    When I search for "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    Then the GitHub search results page is displayed

  @negative
  Scenario: Empty search redirects back to search page or shows all results
    Given I am on the GitHub search page
    When I submit an empty search
    Then the page should not show an application error
