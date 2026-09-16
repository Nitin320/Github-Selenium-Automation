# GitHub Repository Feature
# Author: Sujin
#
# ── How many repos are created per run ───────────────────────────────────────
# @smoke run  (every push):  0 repos — destructive scenarios are excluded
# @regression run (nightly): 3 repos — public + visibility outline (public/private)
# @negative scenarios:       0 repos — validation tests only, no real creation
#
# The second happy-path scenario ("Repository page has expected elements")
# has been merged into the public repository scenario to avoid creating two repos
# for the same thing. Settings is verified within that scenario.
# ─────────────────────────────────────────────────────────────────────────────

@regression
Feature: GitHub Repository Management
  As a GitHub user
  I want to create and manage repositories
  So that I can organise my code on GitHub

  Background:
    Given the user is logged into GitHub

  # ══════════════════════════════════════════════════════════════════════════
  #  DESTRUCTIVE — single repo create, verify title + settings tab, then delete
  #  Excluded from the push smoke gate; runs in regression/manual execution.
  # ══════════════════════════════════════════════════════════════════════════

  @destructive
  Scenario: Create a public repository, verify it, then clean up
    When the user navigates to the new repository page
    And the user creates a new repository with a unique name
    Then the repository should be created and visible
    And the repository settings tab should be accessible
    And the user deletes the repository to clean up

  # ══════════════════════════════════════════════════════════════════════════
  #  NEGATIVE — validation tests (NO repo created, NO cleanup needed)
  # ══════════════════════════════════════════════════════════════════════════

  @negative
  Scenario: Repository creation form requires a name
    When the user navigates to the new repository page
    And the user submits the new repository form with an empty name
    Then the repository form should show a name required error

  @negative
  Scenario: Repository name with only spaces is rejected
    When the user navigates to the new repository page
    And the user submits the new repository form with name "   "
    Then the repository form should show a name required error

  @negative
  Scenario: New repository form is displayed for a logged-in user
    When the user navigates to the new repository page
    Then the new repository form should be displayed

  # ══════════════════════════════════════════════════════════════════════════
  #  REGRESSION ONLY — visibility options (runs nightly, creates 2 repos)
  # ══════════════════════════════════════════════════════════════════════════

  @destructive
  Scenario Outline: Create repositories with different visibility settings
    When the user navigates to the new repository page
    And the user creates a new <visibility> repository with a unique name
    Then the repository should be created and visible
    And the user deletes the repository to clean up

    Examples:
      | visibility |
      | public     |
      | private    |
