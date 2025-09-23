Feature: Matching and Discovery
  As a registered user
  I want to discover and interact with potential matches
  So that I can find meaningful connections

  Background:
    Given the application is running on port 8080
    And I have a valid user registration request
    And I register a new user
    And I login with the registered user credentials

  @matches @discovery
  Scenario: Get potential matches
    When I get potential matches
    Then the response status should be 200
    And I should see a list of potential matches

  @matches @like-user
  Scenario: Like a potential match
    When I like a user with ID "test-user-123"
    Then the response status should be 200
    And the action should be recorded successfully

  @matches @dislike-user
  Scenario: Dislike a potential match
    When I dislike a user with ID "test-user-456"
    Then the response status should be 200
    And the action should be recorded successfully

  @matches @get-matches
  Scenario: Get my matches
    When I get my matches
    Then the response status should be 200
    And I should see my matches list

  @matches @daily-matches
  Scenario: Get daily matches
    When I get my daily matches
    Then the response status should be 200
    And I should see a list of potential matches

  @matches @messaging
  Scenario: Send message to a match
    When I send message "Hello! How are you?" to match "match-123"
    Then the response status should be 200
    And the message should be sent successfully

  @matches @get-messages
  Scenario: Get messages from a match
    When I get messages from match "match-123"
    Then the response status should be 200
    And the response should contain success "true"

  @matches @unmatch
  Scenario: Unmatch with a user
    When I unmatch with user "match-123"
    Then the response status should be 200
    And the unmatch should be successful