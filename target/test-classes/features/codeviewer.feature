# GitHub Code Viewer Feature
# Author: Neil Joe Augustine

@regression
Feature: GitHub Code Viewer
  As a GitHub user
  I want to browse repository files, view file contents, and inspect commit history
  So that I can navigate and understand code hosted on GitHub

  Background:
    Given I am logged in to GitHub

  @smoke
  Scenario: Repository file tree is visible after opening a repository
    When I open the repository "octocat/Hello-World"
    Then the file tree should be displayed

  Scenario: File entry exists in the repository root
    When I open the repository "octocat/Hello-World"
    Then an entry named "README" should exist in the file tree

  @smoke
  Scenario: File content is visible when opening a file
    When I open the file "README" in repository "octocat/Hello-World" on branch "master"
    Then the file content should be visible

  Scenario: Raw content is readable after switching to raw view
    When I open the file "README" in repository "octocat/Hello-World" on branch "master"
    And I click the Raw button
    Then the raw file content should not be empty

  @smoke
  Scenario: Commit history list is visible for a repository
    When I open the commit history for "octocat/Hello-World" on branch "master"
    Then the commit list should be displayed

  Scenario: At least one commit is present in the history
    When I open the commit history for "octocat/Hello-World" on branch "master"
    Then there should be at least 1 commit visible

  Scenario: Commit messages are non-empty
    When I open the commit history for "octocat/Hello-World" on branch "master"
    Then each commit message should not be empty

  Scenario: Commit detail page shows a SHA
    When I open the commit history for "octocat/Hello-World" on branch "master"
    And I click on commit number 1
    Then the commit detail page should display a SHA
