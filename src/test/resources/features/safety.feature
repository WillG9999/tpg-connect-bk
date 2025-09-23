Feature: Safety and Reporting
  As a registered user
  I want to ensure my safety on the platform
  So that I can have a secure dating experience

  Background:
    Given the application is running on port 8080
    And I have a valid user registration request
    And I register a new user
    And I login with the registered user credentials

  @safety @block-user
  Scenario: Block a user
    When I block user with ID "inappropriate-user-123"
    Then the response status should be 200
    And the user should be blocked successfully

  @safety @unblock-user
  Scenario: Unblock a previously blocked user
    Given I block user with ID "blocked-user-456"
    When I unblock user with ID "blocked-user-456"
    Then the response status should be 200
    And the user should be unblocked successfully

  @safety @report-user
  Scenario: Report a user for inappropriate behavior
    When I report user with ID "reported-user-789" for "INAPPROPRIATE_BEHAVIOR"
    Then the response status should be 200
    And the report should be submitted successfully

  @safety @get-blocked-users
  Scenario: Get list of blocked users
    Given I block user with ID "blocked-user-list-test"
    When I get my blocked users
    Then the response status should be 200
    And I should see my blocked users list
    And the blocked user should be in the list

  @safety @safety-blocks
  Scenario: Get safety blocks
    When I get my safety blocks
    Then the response status should be 200
    And the response should contain success "true"

  @safety @create-safety-block
  Scenario: Create a safety block
    When I create a safety block for user "safety-block-user" with reason "HARASSMENT"
    Then the response status should be 200
    And the safety block should be created successfully