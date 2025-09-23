Feature: Health and System Status
  As a system administrator
  I want to check the health status of the application
  So that I can monitor system availability

  Background:
    Given the application is running on port 8080

  @health @system
  Scenario: Check application health
    When I check the application health
    Then the response status should be 200
    And the application should be healthy

  @health @actuator
  Scenario: Check actuator health endpoint
    When I check the actuator health endpoint
    Then the response status should be 200
    And the health status should be "UP"