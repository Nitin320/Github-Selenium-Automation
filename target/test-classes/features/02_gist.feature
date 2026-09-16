# GitHub Gist Feature
# Author: Naveen
#
# ── Session strategy ─────────────────────────────────────────────────────────
# Each scenario uses "the user is logged into GitHub" which hits SessionManager.
# SessionManager caches the session cookies after the FIRST real login (~15 s)
# and injects them into every subsequent driver via cookie restore (~1 s).
# Only ONE real login happens per JVM run regardless of how many scenarios run.
#
# ── Chain strategy for destructive operations ────────────────────────────────
# Edit and delete scenarios CREATE their own gist first, then immediately
# operate on it — all inside a single session.  This means:
#   • No shared test data that could be left dirty by a prior failure.
#   • Only one login for the entire session (SessionManager caches cookies).
#   • Each scenario is fully self-contained and can run independently.
#
# Tags: @smoke = happy-path CI gate | @regression = full nightly suite
#       @negative = error/edge paths | @destructive = creates/deletes real data
# ─────────────────────────────────────────────────────────────────────────────

@regression
Feature: GitHub Gist management

  Background:
    Given the user is logged into GitHub

  # ══════════════════════════════════════════════════════════════════════════
  #  SMOKE — create and verify (runs on every push)
  # ══════════════════════════════════════════════════════════════════════════

  @smoke
  Scenario: Create a public gist
    Given the user navigates to the Gist creation page
    When the user creates a public gist with filename "public_test.txt" and content "This is a public gist"
    Then the gist should be created successfully
    And the gist should be public
    And the gist content should contain "This is a public gist"

  @smoke
  Scenario: Create a secret gist
    Given the user navigates to the Gist creation page
    When the user creates a secret gist with filename "secret_test.txt" and content "This is a secret gist"
    Then the gist should be created successfully
    And the gist should be secret
    And the gist content should contain "This is a secret gist"

  # ══════════════════════════════════════════════════════════════════════════
  #  DATA-DRIVEN — create gists from a table (one login, multiple inputs)
  # ══════════════════════════════════════════════════════════════════════════

  @regression
  Scenario Outline: Create gists with various filenames and content
    Given the user navigates to the Gist creation page
    When the user creates a <visibility> gist with filename "<filename>" and content "<content>"
    Then the gist should be created successfully
    And the gist content should contain "<content>"

    Examples:
      | visibility | filename              | content                         |
      | public     | java_snippet.java     | public class Hello {}           |
      | secret     | notes.md              | # My private markdown notes     |
      | public     | data.csv              | id,name,value                   |
      | secret     | config.json           | env=test debug=true             |

  # ══════════════════════════════════════════════════════════════════════════
  #  CHAINED DESTRUCTIVE — create → edit (same session, same login)
  # ══════════════════════════════════════════════════════════════════════════

  @destructive
  Scenario: Edit a public gist (create then edit in one session)
    Given the user navigates to the Gist creation page
    When the user creates a public gist with filename "edit_public.txt" and content "Original public content"
    And the user edits the gist with content "Updated public content"
    Then the updated gist content should be displayed

  @destructive
  Scenario: Edit a secret gist (create then edit in one session)
    Given the user navigates to the Gist creation page
    When the user creates a secret gist with filename "edit_secret.txt" and content "Original secret content"
    And the user edits the gist with content "Updated secret content"
    Then the updated gist content should be displayed

  # ══════════════════════════════════════════════════════════════════════════
  #  CHAINED DESTRUCTIVE — create → delete (same session, same login)
  # ══════════════════════════════════════════════════════════════════════════

  @destructive
  Scenario: Delete a public gist (create then delete in one session)
    Given the user navigates to the Gist creation page
    When the user creates a public gist with filename "delete_public.txt" and content "Gist to be deleted"
    And the user deletes the gist
    Then the gist should be deleted successfully

  @destructive
  Scenario: Delete a secret gist (create then delete in one session)
    Given the user navigates to the Gist creation page
    When the user creates a secret gist with filename "delete_secret.txt" and content "Secret gist to be deleted"
    And the user deletes the gist
    Then the gist should be deleted successfully

  # ══════════════════════════════════════════════════════════════════════════
  #  CHAINED DESTRUCTIVE — full lifecycle (create → edit → delete)
  # ══════════════════════════════════════════════════════════════════════════

  @destructive
  Scenario: Full lifecycle — create, edit, then delete a public gist
    Given the user navigates to the Gist creation page
    When the user creates a public gist with filename "lifecycle_test.txt" and content "Initial lifecycle content"
    And the user edits the gist with content "Edited lifecycle content"
    Then the updated gist content should be displayed
    When the user deletes the gist
    Then the gist should be deleted successfully

  # ══════════════════════════════════════════════════════════════════════════
  #  NEGATIVE — edge cases and error paths
  # ══════════════════════════════════════════════════════════════════════════

  @negative
  Scenario: Gist creation is blocked when filename is empty
    Given the user navigates to the Gist creation page
    When the user attempts to create a gist with an empty filename and content "Some content"
    Then the gist creation form should indicate a filename is required

  @negative
  Scenario: Gist creation is blocked when content is empty
    Given the user navigates to the Gist creation page
    When the user attempts to create a gist with filename "empty_content.txt" and empty content
    Then the gist creation form should indicate content is required

  @negative
  Scenario: Gist creation page is accessible without JavaScript errors
    Given the user navigates to the Gist creation page
    Then the gist creation form should be fully loaded
