Feature: GitHub Login
  As a GitHub user
  I want to log in and out of my account
  So that I can access my personalized dashboard securely

  Background:
    Given I am on the GitHub login page

  Scenario: Successful login with valid credentials
    When I log in with valid credentials
    Then I should be redirected to my GitHub dashboard
    And the account menu should be visible

  Scenario: Login fails with an invalid username
    When I log in with username "invalid_user_xyz" and password "WrongPassword123!"
    Then I should see a login error message
    And the account menu should not be visible

  Scenario: Login fails with an empty password
    When I log in with a valid username and an empty password
    Then the account menu should not be visible

  Scenario: Logout after a successful login
    Given I have logged in with valid credentials
    When I sign out from my account
    Then the account menu should no longer be visible
