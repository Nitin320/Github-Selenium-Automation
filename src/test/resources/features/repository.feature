# GitHub Repository Feature
# Author: Sujin

@regression
Feature: GitHub Repository Management
  As a GitHub user
  I want to create and manage repositories
  So that I can organise my code on GitHub

  Background:
    Given the user is logged into GitHub

  @smoke @destructive
  Scenario: Create a new public repository
    When the user navigates to the new repository page
    And the user creates a new repository with a unique name
    Then the repository should be created and visible
    And the user deletes the repository to clean up

  @destructive
  Scenario: Repository page has expected elements after creation
    When the user navigates to the new repository page
    And the user creates a new repository with a unique name
    Then the repository settings tab should be accessible
    And the user deletes the repository to clean up
