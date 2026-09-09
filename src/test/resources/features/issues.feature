# GitHub Issues Feature
# Author: Deva Vignan

Feature: GitHub Issues

  Scenario: Open the repository issues page
    Given a GitHub repository is configured
    When I open the repository issues page
    Then the issues page should be displayed

  @issue-creation
  Scenario: Create a new issue
    Given a GitHub repository is configured
    When I open the repository issues page
    And I create an issue with title "Automated issue"
    And I enter the issue description "Issue created by Selenium automation"
    And I submit the issue
    Then the issue should be created successfully
