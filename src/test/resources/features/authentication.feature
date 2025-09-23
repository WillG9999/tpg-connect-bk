Feature: Authentication API
  As a user of the Connect application
  I want to be able to register, login, and manage my authentication
  So that I can securely access the application

  Background:
    Given the application is running on port 8080

  @auth @registration @male
  Scenario: Male user registration with complete profile
    Given I have a male user registration request with complete data
    When I register a new user
    Then the response status should be 200
    And the response should contain success "true"
    And the response should contain an access token
    And the response should contain user information
    And the user should be created in Firebase

  @auth @registration @female  
  Scenario: Female user registration with minimal data
    Given I have a female user registration request with minimal data
    When I register a new user
    Then the response status should be 200
    And the response should contain success "true"
    And the response should contain an access token
    And the response should contain user information
    And the user should be created in Firebase

  @auth @registration @nonbinary
  Scenario: Non-binary user registration from different location
    Given I have a non-binary user registration request from New York
    When I register a new user
    Then the response status should be 200
    And the response should contain success "true"
    And the response should contain an access token
    And the response should contain user information
    And the user should be created in Firebase

  @auth @registration @international
  Scenario: International user registration
    Given I have an international user registration request from London
    When I register a new user
    Then the response status should be 200
    And the response should contain success "true"
    And the response should contain an access token
    And the response should contain user information
    And the user should be created in Firebase

  @auth @registration @senior
  Scenario: Senior user registration
    Given I have a senior user registration request
    When I register a new user
    Then the response status should be 200
    And the response should contain success "true"
    And the response should contain an access token
    And the response should contain user information
    And the user should be created in Firebase

  @auth @registration @duplicate
  Scenario: Duplicate email registration attempt
    Given I have a valid user registration request
    When I register a new user
    Then the response status should be 200
    When I try to register with the same email again
    Then the response status should be 400
    And the response should contain message "Email already registered"

  @auth @login @valid
  Scenario: User login with valid credentials
    Given I have a valid user registration request
    And I register a new user
    When I login with the registered user credentials
    Then the response status should be 200
    And the response should contain success "true"
    And the response should contain an access token
    And the response should contain a refresh token
    And I should be authenticated

  @auth @login @invalid
  Scenario: User login with invalid credentials
    When I login with email "nonexistent@example.com" and password "wrongpassword"
    Then the response status should be 401
    And the response should contain success "false"

  @auth @login @wrongpassword
  Scenario: User login with correct email but wrong password
    Given I have a valid user registration request
    And I register a new user
    When I login with the registered email but wrong password
    Then the response status should be 401
    And the response should contain success "false"

  @auth @refresh @valid
  Scenario: Token refresh with valid refresh token
    Given I have a valid user registration request
    And I register a new user
    And I login with the registered user credentials
    When I refresh my access token
    Then the response status should be 200
    And the response should contain an access token

  @auth @refresh @invalid
  Scenario: Token refresh with invalid refresh token
    Given I have an invalid refresh token
    When I refresh my access token
    Then the response status should be 401

  @auth @logout @valid
  Scenario: User logout with valid session
    Given I have a valid user registration request
    And I register a new user
    And I login with the registered user credentials
    When I logout
    Then the response status should be 200
    And I should not be authenticated

  @auth @password @forgot
  Scenario: Password reset request
    Given I have a valid user registration request
    And I register a new user
    When I request a password reset for the registered email
    Then the response status should be 200
    And the response should contain success "true"

  @auth @password @reset
  Scenario: Password reset with valid token
    Given I have a valid user registration request
    And I register a new user
    And I request a password reset for the registered email
    When I reset password with token and new password "NewSecurePassword123!"
    Then the response status should be 200
    And the response should contain success "true"

  @auth @password @change
  Scenario: Password change with valid current password
    Given I have a valid user registration request
    And I register a new user
    And I login with the registered user credentials
    When I change my password from "SecurePassword123!" to "NewSecurePassword456!"
    Then the response status should be 200
    And the response should contain success "true"

  @auth @account @delete
  Scenario: Account deletion
    Given I have a valid user registration request
    And I register a new user
    And I login with the registered user credentials
    When I delete my account
    Then the response status should be 200
    And the response should contain success "true"

  @auth @verification @email
  Scenario: Email verification with valid token
    Given I have a valid user registration request
    And I register a new user
    When I verify my email with the verification token
    Then the response status should be 200
    And the response should contain success "true"