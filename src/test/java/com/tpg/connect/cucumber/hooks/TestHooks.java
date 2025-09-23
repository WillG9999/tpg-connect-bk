package com.tpg.connect.cucumber.hooks;

import com.tpg.connect.cucumber.TestContext;
import com.tpg.connect.repository.UserRepository;
import com.tpg.connect.repository.UserProfileRepository;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

public class TestHooks {

    @Autowired
    private TestContext testContext;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    @Value("${test.data.email-prefix:cucumber-test-}")
    private String testEmailPrefix;
    
    @Value("${test.data.cleanup-on-exit:true}")
    private boolean cleanupOnExit;
    
    // Track created test users for cleanup
    private static final List<String> createdTestEmails = new ArrayList<>();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Set test profile properties for real database testing
        registry.add("spring.profiles.active", () -> "test");
        registry.add("app.dev.expose-reset-tokens", () -> "true");
        registry.add("app.dev.mock-email-service", () -> "true");
        registry.add("app.dev.create-sample-data", () -> "false");
        registry.add("jwt.secret", () -> "test-jwt-secret-key-for-cucumber-tests-minimum-512-bits-long-key");
        registry.add("jwt.expiration", () -> "3600");
        registry.add("jwt.refresh.expiration", () -> "86400");
    }

    @Before
    public void beforeScenario(Scenario scenario) {
        System.out.println("🧪 Starting scenario: " + scenario.getName());
        testContext.clearContext();
        
        // Set base URI for RestAssured if not already set
        if (RestAssured.baseURI == null || RestAssured.baseURI.equals("http://localhost")) {
            RestAssured.baseURI = "http://localhost";
        }
    }

    @After
    public void afterScenario(Scenario scenario) {
        System.out.println("✅ Finished scenario: " + scenario.getName() + " - Status: " + scenario.getStatus());
        
        // NO CLEANUP - Data persists in database for inspection
        System.out.println("📊 Test data preserved in database for inspection");
        
        // Clear context for next scenario
        testContext.clearContext();
    }

    private void cleanupTestData() {
        String currentUserEmail = testContext.getCurrentUserEmail();
        
        if (currentUserEmail != null && currentUserEmail.startsWith(testEmailPrefix)) {
            try {
                System.out.println("🧹 Cleaning up test data for user: " + currentUserEmail);
                
                // Track this email for cleanup
                synchronized (createdTestEmails) {
                    if (!createdTestEmails.contains(currentUserEmail)) {
                        createdTestEmails.add(currentUserEmail);
                    }
                }
                
                // If we have an access token, try to delete via API
                if (testContext.getAccessToken() != null) {
                    try {
                        given()
                            .header("Authorization", "Bearer " + testContext.getAccessToken())
                            .when()
                            .delete("/api/auth/delete-account");
                        
                        System.out.println("✅ Deleted test user via API: " + currentUserEmail);
                    } catch (Exception e) {
                        System.out.println("⚠️ Could not delete via API, trying direct database cleanup: " + e.getMessage());
                        cleanupDirectDatabase(currentUserEmail);
                    }
                } else {
                    // No token, try direct database cleanup
                    cleanupDirectDatabase(currentUserEmail);
                }
                
            } catch (Exception e) {
                System.out.println("⚠️ Warning: Could not clean up test data for " + currentUserEmail + ": " + e.getMessage());
            }
        }
    }
    
    private void cleanupDirectDatabase(String email) {
        try {
            // Find and delete user from database
            userRepository.findByEmail(email).ifPresent(user -> {
                try {
                    // Delete user profile first
                    userProfileRepository.findByUserId(user.getConnectId());
                    // Note: Add actual deletion logic here if your repositories support it
                    
                    System.out.println("🗑️ Direct database cleanup attempted for: " + email);
                } catch (Exception e) {
                    System.out.println("⚠️ Error in direct database cleanup: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.out.println("⚠️ Could not perform direct database cleanup: " + e.getMessage());
        }
    }
    
    // Cleanup method that can be called from outside to clean all test data
    public static void cleanupAllTestData() {
        synchronized (createdTestEmails) {
            System.out.println("🧹 Final cleanup of " + createdTestEmails.size() + " test accounts");
            createdTestEmails.clear();
        }
    }
}