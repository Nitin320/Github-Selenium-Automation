# GitHub Login Feature
# Author: Jothi Sri
#
# ── Execution order strategy ─────────────────────────────────────────────────
# This file is prefixed "07_" so Cucumber runs it LAST.
# By the time these scenarios execute, all other features (gist, issues,
# repository, search, codeviewer) have already run with the user logged in
# via SessionManager's cookie cache.
#
# Scenario order within this file:
#   1. Verify the user is currently logged in  (confirms session is alive)
#   2. Logout                                  (signs out — ends the session)
#   3. All negative/invalid-credential tests   (run on the login page, logged-out state)
#
# This means ONE real GitHub login for the entire suite run, logout at the
# end, then negative tests — exactly as a real user would do it.
# ─────────────────────────────────────────────────────────────────────────────

@regression
Feature: GitHub Login and Session
  As a GitHub user
  I want to log in and out of my account
  So that I can access my personalized dashboard securely

  # ══════════════════════════════════════════════════════════════════════════
  #  STEP 1 — Verify the active session (we are already logged in from
  #            all the previous feature files that used SessionManager)
  # ══════════════════════════════════════════════════════════════════════════

  @smoke
  Scenario: Confirm the user is logged in before running logout and negative tests
    Given the user is logged into GitHub
    Then I should be redirected to my GitHub dashboard
    And the account menu should be visible

  # ══════════════════════════════════════════════════════════════════════════
  #  STEP 2 — Logout (runs once, after all other features have completed)
  # ══════════════════════════════════════════════════════════════════════════

  @smoke
  Scenario: Logout after completing all authenticated scenarios
    Given the user is logged into GitHub
    When I sign out from my account
    Then the account menu should no longer be visible

  # ══════════════════════════════════════════════════════════════════════════
  #  STEP 3 — Negative tests (all run on the login page in logged-out state)
  #            The Background navigates to the login page each time.
  # ══════════════════════════════════════════════════════════════════════════

  @negative
  Scenario: Login fails with an invalid password
    Given I am on the GitHub login page
    When I log in with a valid username and password "WrongPassword123!"
    Then I should see a login error message
    And the account menu should not be visible

  @negative
  Scenario: Login fails with an empty password
    Given I am on the GitHub login page
    When I log in with a valid username and an empty password
    Then the account menu should not be visible

  @negative
  Scenario: Login fails with an empty username
    Given I am on the GitHub login page
    When I log in with an empty username and a valid password
    Then the account menu should not be visible

  @negative
  Scenario: Login fails with both fields empty
    Given I am on the GitHub login page
    When I log in with an empty username and an empty password
    Then the account menu should not be visible

  # ══════════════════════════════════════════════════════════════════════════
  #  DATA-DRIVEN — invalid credentials (run last, logged-out state)
  # ══════════════════════════════════════════════════════════════════════════

  @negative @regression
  Scenario Outline: Login fails for various invalid credential combinations
    Given I am on the GitHub login page
    When I log in with username "<username>" and password "<password>"
    Then I should see a login error message

    Examples:
      | username         | password          |
      | not-a-real-user  | SomePassword1!    |
      | valid@email.com  | wrongpass         |
      | admin            | admin             |
      | root             | root              |
