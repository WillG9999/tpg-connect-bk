Feature: User Profile Management
  As a registered user
  I want to manage my profile information
  So that I can customize my dating profile

  Background:
    Given the application is running on port 8080
    And I have a valid user registration request
    And I register a new user
    And I login with the registered user credentials

  @profile @get-profile
  Scenario: Get current user profile
    When I get my current profile
    Then the response status should be 200
    And the response should contain success "true"
    And the profile should contain basic information

  @profile @update-basic
  Scenario: Update basic profile information
    When I update my basic profile information
    Then the response status should be 200
    And the profile should be updated successfully
    
  @profile @update-bio
  Scenario: Add bio to profile
    When I update my basic info with bio "I love hiking and photography"
    Then the response status should be 200
    And the profile should be updated successfully

  @profile @update-preferences
  Scenario: Update profile preferences
    When I update my profile preferences
    Then the response status should be 200
    And the profile should be updated successfully

  @profile @field-visibility
  Scenario: Update field visibility settings
    When I update my field visibility settings
    Then the response status should be 200
    And the profile should be updated successfully

  @profile @written-prompts
  Scenario: Add written prompts
    When I add a written prompt with question "What makes you laugh?" and answer "Good comedy movies"
    Then the response status should be 200
    And the profile should be updated successfully

  @profile @photos
  Scenario: Update photos order
    When I update my photos order
    Then the response status should be 200
    And the profile should be updated successfully