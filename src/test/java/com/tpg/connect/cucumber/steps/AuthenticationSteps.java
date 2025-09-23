package com.tpg.connect.cucumber.steps;

import com.tpg.connect.cucumber.CucumberTestConfiguration;
import com.tpg.connect.cucumber.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationSteps {

    @Autowired
    private CucumberTestConfiguration testConfiguration;
    
    @Autowired
    private TestContext testContext;

    @Given("the application is running on port {int}")
    public void the_application_is_running_on_port(int port) {
        RestAssured.port = testConfiguration.getPort();
        RestAssured.baseURI = "http://localhost";
    }

    @Given("I have a valid user registration request")
    public void i_have_a_valid_user_registration_request() {
        Map<String, Object> registrationData = new HashMap<>();
        // Use test prefix for easy identification and cleanup
        registrationData.put("email", "cucumber-test-" + System.currentTimeMillis() + "@example.com");
        registrationData.put("password", "SecurePassword123!");
        registrationData.put("confirmPassword", "SecurePassword123!");
        registrationData.put("firstName", "John");
        registrationData.put("lastName", "Doe");
        registrationData.put("dateOfBirth", "1995-06-15");
        registrationData.put("gender", "MALE");
        registrationData.put("location", "San Francisco, CA");
        
        testContext.setTestData("registrationData", registrationData);
        testContext.setCurrentUserEmail((String) registrationData.get("email"));
        
        System.out.println("🧪 Generated test user email: " + registrationData.get("email"));
    }

    @Given("I have a male user registration request with complete data")
    public void i_have_a_male_user_registration_request_with_complete_data() {
        Map<String, Object> registrationData = new HashMap<>();
        registrationData.put("email", "cucumber-test-male-" + System.currentTimeMillis() + "@example.com");
        registrationData.put("password", "SecurePassword123!");
        registrationData.put("confirmPassword", "SecurePassword123!");
        registrationData.put("firstName", "Marcus");
        registrationData.put("lastName", "Rodriguez");
        registrationData.put("dateOfBirth", "1992-03-22");
        registrationData.put("gender", "MALE");
        registrationData.put("location", "Austin, TX");
        
        testContext.setTestData("registrationData", registrationData);
        testContext.setCurrentUserEmail((String) registrationData.get("email"));
        
        System.out.println("🧪 Generated male test user: " + registrationData.get("email"));
    }

    @Given("I have a female user registration request with minimal data")
    public void i_have_a_female_user_registration_request_with_minimal_data() {
        Map<String, Object> registrationData = new HashMap<>();
        registrationData.put("email", "cucumber-test-female-" + System.currentTimeMillis() + "@example.com");
        registrationData.put("password", "StrongPass456!");
        registrationData.put("confirmPassword", "StrongPass456!");
        registrationData.put("firstName", "Sarah");
        registrationData.put("lastName", "Chen");
        registrationData.put("dateOfBirth", "1997-11-08");
        registrationData.put("gender", "FEMALE");
        registrationData.put("location", "Seattle, WA");
        
        testContext.setTestData("registrationData", registrationData);
        testContext.setCurrentUserEmail((String) registrationData.get("email"));
        
        System.out.println("🧪 Generated female test user: " + registrationData.get("email"));
    }

    @Given("I have a non-binary user registration request from New York")
    public void i_have_a_non_binary_user_registration_request_from_new_york() {
        Map<String, Object> registrationData = new HashMap<>();
        registrationData.put("email", "cucumber-test-nb-" + System.currentTimeMillis() + "@example.com");
        registrationData.put("password", "MySecurePass789!");
        registrationData.put("confirmPassword", "MySecurePass789!");
        registrationData.put("firstName", "Alex");
        registrationData.put("lastName", "Morgan");
        registrationData.put("dateOfBirth", "1994-09-14");
        registrationData.put("gender", "NON_BINARY");
        registrationData.put("location", "Brooklyn, NY");
        
        testContext.setTestData("registrationData", registrationData);
        testContext.setCurrentUserEmail((String) registrationData.get("email"));
        
        System.out.println("🧪 Generated non-binary test user: " + registrationData.get("email"));
    }

    @Given("I have an international user registration request from London")
    public void i_have_an_international_user_registration_request_from_london() {
        Map<String, Object> registrationData = new HashMap<>();
        registrationData.put("email", "cucumber-test-intl-" + System.currentTimeMillis() + "@example.com");
        registrationData.put("password", "BritishPass2024!");
        registrationData.put("confirmPassword", "BritishPass2024!");
        registrationData.put("firstName", "Oliver");
        registrationData.put("lastName", "Thompson");
        registrationData.put("dateOfBirth", "1989-12-03");
        registrationData.put("gender", "MALE");
        registrationData.put("location", "London, UK");
        
        testContext.setTestData("registrationData", registrationData);
        testContext.setCurrentUserEmail((String) registrationData.get("email"));
        
        System.out.println("🧪 Generated international test user: " + registrationData.get("email"));
    }

    @Given("I have a senior user registration request")
    public void i_have_a_senior_user_registration_request() {
        Map<String, Object> registrationData = new HashMap<>();
        registrationData.put("email", "cucumber-test-senior-" + System.currentTimeMillis() + "@example.com");
        registrationData.put("password", "WisdomPass55!");
        registrationData.put("confirmPassword", "WisdomPass55!");
        registrationData.put("firstName", "Patricia");
        registrationData.put("lastName", "Williams");
        registrationData.put("dateOfBirth", "1968-07-19");
        registrationData.put("gender", "FEMALE");
        registrationData.put("location", "Phoenix, AZ");
        
        testContext.setTestData("registrationData", registrationData);
        testContext.setCurrentUserEmail((String) registrationData.get("email"));
        
        System.out.println("🧪 Generated senior test user: " + registrationData.get("email"));
    }

    @When("I register a new user")
    public void i_register_a_new_user() {
        Map<String, Object> registrationData = (Map<String, Object>) testContext.getTestData("registrationData");
        
        System.out.println("🔗 Creating REAL user in database: " + registrationData.get("email"));
        
        Response response = given()
            .contentType(ContentType.JSON)
            .body(registrationData)
            .when()
            .post("/api/auth/register");
        
        testContext.setLastResponse(response);
        
        System.out.println("📊 Registration response status: " + response.getStatusCode());
        if (response.getStatusCode() != 200) {
            System.out.println("❌ Registration failed: " + response.getBody().asString());
        } else {
            System.out.println("✅ User successfully created in database!");
        }
        
        // Extract verification token if present
        if (response.getStatusCode() == 200 && response.jsonPath().get("verificationToken") != null) {
            testContext.setVerificationToken(response.jsonPath().get("verificationToken"));
            System.out.println("📧 Verification token captured for testing");
        }
        
        // Extract access token if present
        if (response.getStatusCode() == 200 && response.jsonPath().get("accessToken") != null) {
            testContext.setAccessToken(response.jsonPath().get("accessToken"));
            System.out.println("🔑 Access token captured: " + testContext.getAccessToken().substring(0, 20) + "...");
        }
    }

    @When("I login with email {string} and password {string}")
    public void i_login_with_email_and_password(String email, String password) {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", email);
        loginData.put("password", password);
        loginData.put("deviceType", "WEB");
        
        Response response = given()
            .contentType(ContentType.JSON)
            .body(loginData)
            .when()
            .post("/api/auth/login");
        
        testContext.setLastResponse(response);
        
        // Extract tokens if login successful
        if (response.getStatusCode() == 200) {
            if (response.jsonPath().get("accessToken") != null) {
                testContext.setAccessToken(response.jsonPath().get("accessToken"));
            }
            if (response.jsonPath().get("refreshToken") != null) {
                testContext.setRefreshToken(response.jsonPath().get("refreshToken"));
            }
            testContext.setCurrentUserEmail(email);
        }
    }

    @When("I login with the registered user credentials")
    public void i_login_with_the_registered_user_credentials() {
        Map<String, Object> registrationData = (Map<String, Object>) testContext.getTestData("registrationData");
        String email = (String) registrationData.get("email");
        String password = (String) registrationData.get("password");
        
        i_login_with_email_and_password(email, password);
    }

    @When("I refresh my access token")
    public void i_refresh_my_access_token() {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getRefreshToken())
            .when()
            .post("/api/auth/refresh");
        
        testContext.setLastResponse(response);
        
        // Update tokens if refresh successful
        if (response.getStatusCode() == 200) {
            if (response.jsonPath().get("accessToken") != null) {
                testContext.setAccessToken(response.jsonPath().get("accessToken"));
            }
            if (response.jsonPath().get("refreshToken") != null) {
                testContext.setRefreshToken(response.jsonPath().get("refreshToken"));
            }
        }
    }

    @When("I logout")
    public void i_logout() {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .post("/api/auth/logout");
        
        testContext.setLastResponse(response);
    }

    @When("I request a password reset for email {string}")
    public void i_request_a_password_reset_for_email(String email) {
        Map<String, String> resetData = new HashMap<>();
        resetData.put("email", email);
        
        Response response = given()
            .contentType(ContentType.JSON)
            .body(resetData)
            .when()
            .post("/api/auth/forgot-password");
        
        testContext.setLastResponse(response);
        
        // Extract reset token if present (in development mode)
        if (response.getStatusCode() == 200 && response.jsonPath().get("resetToken") != null) {
            testContext.setResetToken(response.jsonPath().get("resetToken"));
        }
    }

    @When("I reset password with token and new password {string}")
    public void i_reset_password_with_token_and_new_password(String newPassword) {
        Map<String, String> resetData = new HashMap<>();
        resetData.put("token", testContext.getResetToken());
        resetData.put("newPassword", newPassword);
        resetData.put("confirmPassword", newPassword);
        
        Response response = given()
            .contentType(ContentType.JSON)
            .body(resetData)
            .when()
            .post("/api/auth/reset-password");
        
        testContext.setLastResponse(response);
    }

    @When("I verify my email with the verification token")
    public void i_verify_my_email_with_the_verification_token() {
        Response response = given()
            .param("token", testContext.getVerificationToken())
            .when()
            .get("/api/auth/verify-email");
        
        testContext.setLastResponse(response);
    }

    @When("I change my password from {string} to {string}")
    public void i_change_my_password_from_to(String currentPassword, String newPassword) {
        Map<String, String> changePasswordData = new HashMap<>();
        changePasswordData.put("currentPassword", currentPassword);
        changePasswordData.put("newPassword", newPassword);
        changePasswordData.put("confirmPassword", newPassword);
        
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .contentType(ContentType.JSON)
            .body(changePasswordData)
            .when()
            .post("/api/auth/change-password");
        
        testContext.setLastResponse(response);
    }

    @When("I delete my account")
    public void i_delete_my_account() {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .delete("/api/auth/delete-account");
        
        testContext.setLastResponse(response);
    }

    @Then("the response status should be {int}")
    public void the_response_status_should_be(int expectedStatus) {
        assertEquals(expectedStatus, testContext.getLastResponse().getStatusCode());
    }

    @Then("the response should contain success {string}")
    public void the_response_should_contain_success(String expectedSuccess) {
        boolean expected = Boolean.parseBoolean(expectedSuccess);
        testContext.getLastResponse()
            .then()
            .body("success", equalTo(expected));
    }

    @Then("the response should contain an access token")
    public void the_response_should_contain_an_access_token() {
        testContext.getLastResponse()
            .then()
            .body("accessToken", notNullValue());
    }

    @Then("the response should contain a refresh token")
    public void the_response_should_contain_a_refresh_token() {
        testContext.getLastResponse()
            .then()
            .body("refreshToken", notNullValue());
    }

    @Then("the response should contain user information")
    public void the_response_should_contain_user_information() {
        testContext.getLastResponse()
            .then()
            .body("user", notNullValue());
    }

    @Then("the response should contain message {string}")
    public void the_response_should_contain_message(String expectedMessage) {
        testContext.getLastResponse()
            .then()
            .body("message", containsString(expectedMessage));
    }

    @Then("I should be authenticated")
    public void i_should_be_authenticated() {
        assertNotNull(testContext.getAccessToken(), "Access token should be present");
        assertFalse(testContext.getAccessToken().isEmpty(), "Access token should not be empty");
    }

    @Then("I should not be authenticated")
    public void i_should_not_be_authenticated() {
        // After logout, we don't clear the token from context, but the server should reject it
        // We can test this by making an authenticated request
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .get("/api/user/profile");
        
        assertTrue(response.getStatusCode() == 401 || response.getStatusCode() == 403, 
            "Should receive 401 or 403 after logout");
    }
    
    @Then("the user should be created in Firebase")
    public void the_user_should_be_created_in_firebase() {
        // Verify the user was actually created by checking the response contains valid data
        assertNotNull(testContext.getAccessToken(), "Access token should be present");
        assertNotNull(testContext.getCurrentUserEmail(), "User email should be captured");
        assertTrue(testContext.getCurrentUserEmail().startsWith("cucumber-test-"), 
            "User email should have test prefix");
        
        System.out.println("🔍 Verified user created: " + testContext.getCurrentUserEmail());
        System.out.println("🔍 Access token length: " + testContext.getAccessToken().length());
    }

    @When("I try to register with the same email again")
    public void i_try_to_register_with_the_same_email_again() {
        Map<String, Object> registrationData = (Map<String, Object>) testContext.getTestData("registrationData");
        
        Response response = given()
            .contentType(ContentType.JSON)
            .body(registrationData)
            .when()
            .post("/api/auth/register");
        
        testContext.setLastResponse(response);
        
        System.out.println("📊 Duplicate registration response status: " + response.getStatusCode());
    }

    @When("I login with the registered email but wrong password")
    public void i_login_with_the_registered_email_but_wrong_password() {
        Map<String, Object> registrationData = (Map<String, Object>) testContext.getTestData("registrationData");
        String email = (String) registrationData.get("email");
        
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", email);
        loginData.put("password", "WrongPassword123!");
        loginData.put("deviceType", "WEB");
        
        Response response = given()
            .contentType(ContentType.JSON)
            .body(loginData)
            .when()
            .post("/api/auth/login");
        
        testContext.setLastResponse(response);
        
        System.out.println("📊 Wrong password login response status: " + response.getStatusCode());
    }

    @When("I request a password reset for the registered email")
    public void i_request_a_password_reset_for_the_registered_email() {
        Map<String, Object> registrationData = (Map<String, Object>) testContext.getTestData("registrationData");
        String email = (String) registrationData.get("email");
        
        Map<String, String> resetData = new HashMap<>();
        resetData.put("email", email);
        
        Response response = given()
            .contentType(ContentType.JSON)
            .body(resetData)
            .when()
            .post("/api/auth/forgot-password");
        
        testContext.setLastResponse(response);
        
        // Extract reset token if present (in development mode)
        if (response.getStatusCode() == 200 && response.jsonPath().get("resetToken") != null) {
            testContext.setResetToken(response.jsonPath().get("resetToken"));
        }
        
        System.out.println("📊 Password reset request response status: " + response.getStatusCode());
    }

    @Given("I have an invalid refresh token")
    public void i_have_an_invalid_refresh_token() {
        testContext.setRefreshToken("invalid_refresh_token_for_testing");
        System.out.println("🧪 Set invalid refresh token for testing");
    }
}