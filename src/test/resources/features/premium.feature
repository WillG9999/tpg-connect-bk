Feature: Premium Subscription Management
  As a registered user
  I want to manage my premium subscription
  So that I can access premium features

  Background:
    Given the application is running on port 8080
    And I have a valid user registration request
    And I register a new user
    And I login with the registered user credentials

  @premium @get-status
  Scenario: Get premium status
    When I get my premium status
    Then the response status should be 200
    And the response should contain success "true"
    And the response should contain premium status

  @premium @subscription-plans
  Scenario: Get subscription plans
    When I get available subscription plans
    Then the response status should be 200
    And the response should contain success "true"
    And I should see available subscription plans

  @premium @create-subscription
  Scenario: Create a new subscription
    When I create a subscription with plan "premium_monthly"
    Then the response status should be 200
    And the subscription should be created successfully

  @premium @get-subscription
  Scenario: Get current subscription
    When I get my current subscription
    Then the response status should be 200
    And the response should contain success "true"

  @premium @subscription-history
  Scenario: Get subscription history
    When I get my subscription history
    Then the response status should be 200
    And the response should contain success "true"
    And I should see my subscription history

  @premium @cancel-subscription
  Scenario: Cancel subscription
    Given I create a subscription with plan "premium_monthly"
    When I cancel my subscription
    Then the response status should be 200
    And the subscription should be cancelled successfully

  @premium @restore-subscription
  Scenario: Restore subscription
    When I restore my subscription
    Then the response status should be 200
    And the response should contain success "true"