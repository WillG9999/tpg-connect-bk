Feature: Conversations Management
  As a matched user
  I want to manage my conversations
  So that I can communicate with my matches

  Background:
    Given the application is running on port 8080
    And I have a valid user registration request
    And I register a new user
    And I login with the registered user credentials

  @conversations @get-conversations
  Scenario: Get all conversations
    When I get all my conversations
    Then the response status should be 200
    And the response should contain success "true"
    And I should see my conversations list

  @conversations @get-conversation
  Scenario: Get specific conversation
    When I get conversation "conversation-123"
    Then the response status should be 200
    And the response should contain success "true"
    And I should see the conversation details

  @conversations @unmatch-conversation
  Scenario: Unmatch a conversation
    When I unmatch conversation "conversation-456"
    Then the response status should be 200
    And the conversation should be unmatched successfully

  @conversations @conversation-summary
  Scenario: Get conversation summary
    When I get conversation summary for "conversation-789"
    Then the response status should be 200
    And the response should contain success "true"
    And I should see the conversation summary