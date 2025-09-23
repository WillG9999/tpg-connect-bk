Feature: Notifications Management
  As a registered user
  I want to manage my notifications
  So that I can stay informed about platform activities

  Background:
    Given the application is running on port 8080
    And I have a valid user registration request
    And I register a new user
    And I login with the registered user credentials

  @notifications @get-notifications
  Scenario: Get user notifications
    When I get my notifications
    Then the response status should be 200
    And the response should contain success "true"
    And I should see my notifications list

  @notifications @unread-count
  Scenario: Get unread notifications count
    When I get my unread notifications count
    Then the response status should be 200
    And the response should contain success "true"
    And the response should contain unread count

  @notifications @mark-as-read
  Scenario: Mark notification as read
    When I mark notification "notification-123" as read
    Then the response status should be 200
    And the notification should be marked as read

  @notifications @mark-all-read
  Scenario: Mark all notifications as read
    When I mark all notifications as read
    Then the response status should be 200
    And all notifications should be marked as read

  @notifications @delete-notification
  Scenario: Delete a specific notification
    When I delete notification "notification-456"
    Then the response status should be 200
    And the notification should be deleted successfully

  @notifications @delete-all
  Scenario: Delete all notifications
    When I delete all my notifications
    Then the response status should be 200
    And all notifications should be deleted successfully