# GitHub Search Feature
# Author: Yazeen

Feature: GitHub Search and Explore

  Scenario: Search GitHub repositories
    Given I am on the GitHub search page
    When I search for "selenium"
    Then the GitHub search results page is displayed
    And the search results contain "selenium"

  Scenario: Explore trending repositories
    Given I am on the GitHub Explore page
    Then the GitHub Explore page is displayed
    When I open trending repositories
    Then the GitHub trending page is displayed
