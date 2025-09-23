package com.tpg.connect.cucumber.steps;

import com.tpg.connect.cucumber.TestContext;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class PremiumSteps {

    @Autowired
    private TestContext testContext;

    @When("I get my premium status")
    public void i_get_my_premium_status() {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .get("/api/premium/status");
        
        testContext.setLastResponse(response);
    }

    @When("I get available subscription plans")
    public void i_get_available_subscription_plans() {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .get("/api/premium/plans");
        
        testContext.setLastResponse(response);
    }

    @When("I create a subscription with plan {string}")
    public void i_create_a_subscription_with_plan(String planId) {
        Map<String, Object> subscriptionData = new HashMap<>();
        subscriptionData.put("planId", planId);
        subscriptionData.put("paymentMethodId", "test_payment_method");
        subscriptionData.put("autoRenew", true);
        
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .contentType(ContentType.JSON)
            .body(subscriptionData)
            .when()
            .post("/api/premium/subscribe");
        
        testContext.setLastResponse(response);
        testContext.setTestData("subscriptionPlan", planId);
    }

    @When("I get my current subscription")
    public void i_get_my_current_subscription() {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .get("/api/premium/subscription");
        
        testContext.setLastResponse(response);
    }

    @When("I get my subscription history")
    public void i_get_my_subscription_history() {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .get("/api/premium/history");
        
        testContext.setLastResponse(response);
    }

    @When("I cancel my subscription")
    public void i_cancel_my_subscription() {
        Map<String, String> cancelData = new HashMap<>();
        cancelData.put("reason", "No longer needed");
        
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .contentType(ContentType.JSON)
            .body(cancelData)
            .when()
            .post("/api/premium/cancel");
        
        testContext.setLastResponse(response);
    }

    @When("I restore my subscription")
    public void i_restore_my_subscription() {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .post("/api/premium/restore");
        
        testContext.setLastResponse(response);
    }

    @Then("the response should contain premium status")
    public void the_response_should_contain_premium_status() {
        testContext.getLastResponse()
            .then()
            .body("isPremium", notNullValue())
            .body("premiumExpiry", notNullValue());
    }

    @Then("I should see available subscription plans")
    public void i_should_see_available_subscription_plans() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("plans", notNullValue())
            .body("plans.size()", greaterThan(0));
    }

    @Then("the subscription should be created successfully")
    public void the_subscription_should_be_created_successfully() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("subscriptionId", notNullValue());
    }

    @Then("I should see my subscription history")
    public void i_should_see_my_subscription_history() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("subscriptions", notNullValue());
    }

    @Then("the subscription should be cancelled successfully")
    public void the_subscription_should_be_cancelled_successfully() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("message", containsString("cancelled"));
    }
}