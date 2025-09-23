# Cucumber Integration Tests for Connect Backend

This directory contains Cucumber BDD (Behavior-Driven Development) tests for the Connect dating application backend.

## Test Structure

### Feature Files (`features/`)
- `authentication.feature` - User registration, login, logout, password management
- `profile.feature` - User profile management and updates
- `matches.feature` - Matching, discovery, and user interactions
- `safety.feature` - User safety features (blocking, reporting)
- `notifications.feature` - Notification management
- `premium.feature` - Premium subscription management
- `conversations.feature` - Conversation and messaging features
- `health.feature` - System health and status checks

### Step Definitions (`src/test/java/com/tpg/connect/cucumber/steps/`)
- `AuthenticationSteps.java` - Authentication-related step implementations
- `ProfileSteps.java` - Profile management step implementations
- `MatchSteps.java` - Matching and discovery step implementations
- `SafetySteps.java` - Safety feature step implementations
- `NotificationSteps.java` - Notification step implementations
- `PremiumSteps.java` - Premium subscription step implementations
- `ConversationSteps.java` - Conversation step implementations
- `HealthSteps.java` - Health check step implementations

### Configuration
- `CucumberTestRunner.java` - Main test runner configuration
- `CucumberTestConfiguration.java` - Spring Boot test configuration
- `TestContext.java` - Shared context for test data and state
- `TestHooks.java` - Before/after scenario hooks and cleanup

## Running Tests

### Run All Cucumber Tests
```bash
mvn clean verify
```

### Run Specific Tags
```bash
mvn clean verify -Dcucumber.filter.tags="@auth"
mvn clean verify -Dcucumber.filter.tags="@profile"
mvn clean verify -Dcucumber.filter.tags="@matches"
mvn clean verify -Dcucumber.filter.tags="@safety"
```

### Run Unit Tests Only
```bash
mvn clean test
```

### Run Integration Tests Only
```bash
mvn clean integration-test
```

## Test Tags

Tests are organized with the following tags:

### Authentication (`@auth`)
- `@registration` - User registration tests
- `@login` - User login tests
- `@token-refresh` - Token refresh tests
- `@logout` - User logout tests
- `@password-reset` - Password reset flow tests
- `@email-verification` - Email verification tests
- `@change-password` - Password change tests
- `@account-deletion` - Account deletion tests
- `@negative` - Negative test cases

### Profile (`@profile`)
- `@get-profile` - Get profile tests
- `@update-basic` - Basic info update tests
- `@update-preferences` - Preference update tests
- `@field-visibility` - Field visibility tests
- `@written-prompts` - Written prompts tests
- `@photos` - Photo management tests

### Matches (`@matches`)
- `@discovery` - Discovery tests
- `@like-user` - Like action tests
- `@dislike-user` - Dislike action tests
- `@get-matches` - Get matches tests
- `@messaging` - Messaging tests
- `@unmatch` - Unmatch tests

### Safety (`@safety`)
- `@block-user` - User blocking tests
- `@unblock-user` - User unblocking tests
- `@report-user` - User reporting tests
- `@get-blocked-users` - Get blocked users tests

### Other Tags
- `@notifications` - Notification tests
- `@premium` - Premium subscription tests
- `@conversations` - Conversation tests
- `@health` - Health check tests
- `@ignore` - Tests to be ignored

## Test Data Management

- Tests use randomly generated email addresses to avoid conflicts
- Each scenario gets a fresh test context
- Test data is cleaned up after each scenario
- Authentication tokens are managed automatically
- Test-specific configuration uses `application-test.properties`

## Test Environment

The tests run in a test profile with:
- Mock email service enabled
- Development features enabled for token exposure
- Detailed error responses
- Custom JWT secrets for testing
- Disabled unnecessary cloud services

## Reports

Test results are generated in:
- `target/cucumber-reports/` - HTML reports
- `target/cucumber-reports/Cucumber.json` - JSON reports
- Console output with pretty formatting

## Extending Tests

### Adding New Feature Files
1. Create a new `.feature` file in `src/test/resources/features/`
2. Write scenarios using Gherkin syntax
3. Add appropriate tags
4. Create corresponding step definitions

### Adding New Step Definitions
1. Create a new step definition class in the `steps` package
2. Inject `TestContext` for state management
3. Use RestAssured for HTTP requests
4. Add appropriate assertions

### Adding New Test Data
1. Use the `TestContext` to store test data
2. Generate dynamic data to avoid conflicts
3. Clean up data in test hooks
4. Use realistic test data that matches production patterns

## Best Practices

1. **Use meaningful scenario names** that describe the business value
2. **Keep scenarios focused** on a single feature or user story
3. **Use tags effectively** for test organization and execution
4. **Maintain test independence** - each scenario should be able to run alone
5. **Clean up test data** to prevent interference between tests
6. **Use realistic test data** that reflects actual usage patterns
7. **Write readable Gherkin** that stakeholders can understand

## Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure the application is not running when tests start
2. **Database conflicts**: Tests use isolated test data
3. **Authentication failures**: Check JWT configuration in test properties
4. **Firebase errors**: Mock services are enabled for testing
5. **Test data cleanup**: Verify hooks are running properly

### Debug Mode

Enable debug logging by setting:
```properties
logging.level.com.tpg.connect=DEBUG
logging.level.io.cucumber=DEBUG
```

### Test Isolation

If tests are interfering with each other:
1. Check test hooks are cleaning up properly
2. Verify unique test data generation
3. Ensure proper use of test context
4. Check for shared state issues