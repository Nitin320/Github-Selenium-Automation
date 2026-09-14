# GitHub Gist Feature
# Author: Naveen

@regression
Feature: GitHub Gist management

  Background:
    Given the user is logged into GitHub

  @smoke
  Scenario: Create a public gist
    Given the user navigates to the Gist creation page
    When the user creates a public gist with filename "public_test.txt" and content "This is a public gist"
    Then the gist should be created successfully
    And the gist should be public
    And the gist content should contain "This is a public gist"

  Scenario: Create a secret gist
    Given the user navigates to the Gist creation page
    When the user creates a secret gist with filename "secret_test.txt" and content "This is a secret gist"
    Then the gist should be created successfully
    And the gist should be secret
    And the gist content should contain "This is a secret gist"

  @destructive
  Scenario: Edit a public gist
    Given the user navigates to the Gist creation page
    When the user creates a public gist with filename "edit_public.txt" and content "Original public content"
    And the user edits the gist with content "Updated public content"
    Then the updated gist content should be displayed

  @destructive
  Scenario: Edit a secret gist
    Given the user navigates to the Gist creation page
    When the user creates a secret gist with filename "edit_secret.txt" and content "Original secret content"
    And the user edits the gist with content "Updated secret content"
    Then the updated gist content should be displayed

  @destructive
  Scenario: Delete a public gist
    Given the user navigates to the Gist creation page
    When the user creates a public gist with filename "delete_public.txt" and content "Gist to be deleted"
    And the user deletes the gist
    Then the gist should be deleted successfully

  @destructive
  Scenario: Delete a secret gist
    Given the user navigates to the Gist creation page
    When the user creates a secret gist with filename "delete_secret.txt" and content "Secret gist to be deleted"
    And the user deletes the gist
    Then the gist should be deleted successfully
