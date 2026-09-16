# GitHub Autonomous Explorer Feature
# Author: Group 5 — Autonomous AI Engine

@regression
Feature: Autonomous AI-Driven Page Exploration
  As a QA engineer
  I want the autonomous engine to crawl GitHub pages and sample other users' repos
  So I get real data on what loads, where redirects go, and what elements exist

  # ── How the explorer works ────────────────────────────────────────────────
  # 1. Starts at the given page and scans all links + buttons.
  # 2. Follows github.com-internal links up to explorer.max.depth hops.
  # 3. On pages like Explore/Trending that show OTHER users' repos — the
  #    explorer visits up to explorer.sample.repos (default 2) of them,
  #    loads the repo page, records its title / buttons / links, then stops
  #    there without going deeper inside that repo.
  # 4. Any link that would leave github.com entirely (e.g. a project website)
  #    is RECORDED with: source page + link text + destination URL — so the
  #    report clearly shows "at this GitHub page, clicking this link would take
  #    the user to this external site". The browser never actually follows it.
  # 5. Danger keywords (delete/destroy/archive/disable/revoke) — elements are
  #    listed in the report but never clicked.
  # ─────────────────────────────────────────────────────────────────────────

  Background:
    Given the user is logged into GitHub

  @smoke
  Scenario: Autonomous goal — navigate and assert
    When the autonomous engine runs the goal "navigate to /dashboard then assert url contains github.com"
    Then the autonomous run should pass

  @smoke
  Scenario: Autonomous goal — navigate to settings
    When the autonomous engine runs the goal "navigate to /settings then assert url contains settings"
    Then the autonomous run should pass

  @smoke
  Scenario: Autonomous planner chains multiple tasks
    When the autonomous engine runs the goal "navigate to /explore then assert url contains explore then navigate to /trending then assert url contains trending"
    Then the autonomous run should pass

  @regression
  Scenario: Explorer crawls the dashboard and records all findings
    When the explorer scans the GitHub dashboard page
    Then the explorer report should be generated
    And the explorer should have visited at least 1 page

  @regression
  Scenario: Explorer visits the Explore page and samples other users' repos
    When the explorer scans the GitHub explore page
    Then the explorer report should be generated
    And the explorer should have sampled at least 1 repo
    And the sampled repos should have loaded successfully

  @regression
  Scenario: Explorer scans the configured test repository
    When the explorer scans the repository page for the configured test repo
    Then the explorer report should be generated
    And the explorer should have visited at least 1 page
