# GitHub Code Viewer Feature
# Author: Neil Joe Augustine
#
# ── Consolidation note ───────────────────────────────────────────────────────
# All code-viewer checks are combined into ONE scenario per concern so the
# browser navigates to each URL exactly once instead of once per assertion.
#
#  Before: 8 scenarios × 1 navigation each = 8 trips to GitHub
#  After : 3 scenarios × 1 navigation each = 3 trips to GitHub
# ─────────────────────────────────────────────────────────────────────────────

@regression
Feature: GitHub Code Viewer
  As a GitHub user
  I want to browse repository files, view file contents, and inspect commit history
  So that I can navigate and understand code hosted on GitHub

  Background:
    Given the user is logged into GitHub

  # Opens the repo ONCE — checks tree visible + README entry in one trip
  @smoke
  Scenario: Repository file tree is visible and contains expected files
    When I open the repository "octocat/Hello-World"
    Then the file tree should be displayed
    And an entry named "README" should exist in the file tree

  # Opens the file ONCE — checks content visible + raw content in one trip
  @smoke
  Scenario: File content and raw view are accessible
    When I open the file "README" in repository "octocat/Hello-World" on branch "master"
    Then the file content should be visible
    When I click the Raw button
    Then the raw file content should not be empty

  # Opens commits ONCE — checks list, count, messages, then clicks into detail SHA
  @smoke
  Scenario: Commit history is visible and commit detail shows a SHA
    When I open the commit history for "octocat/Hello-World" on branch "master"
    Then the commit list should be displayed
    And there should be at least 1 commit visible
    And each commit message should not be empty
    When I click on commit number 1
    Then the commit detail page should display a SHA
