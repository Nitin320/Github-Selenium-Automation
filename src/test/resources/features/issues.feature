# GitHub Issues Feature
# Author: Deva Vignan

@regression
Feature: GitHub Issues

  Background:
    Given the user is logged into GitHub
    And a GitHub repository is configured

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
