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

public class SafetySteps {

    @Autowired
    private TestContext testContext;

    @When("I block user with ID {string}")
    public void i_block_user_with_id(String userId) {
        Map<String, String> blockData = new HashMap<>();
        blockData.put("blockedUserId", userId);
        blockData.put("reason", "INAPPROPRIATE_BEHAVIOR");
        
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .contentType(ContentType.JSON)
            .body(blockData)
            .when()
            .post("/api/safety/block");
        
        testContext.setLastResponse(response);
        testContext.setTestData("blockedUserId", userId);
    }

    @When("I unblock user with ID {string}")
    public void i_unblock_user_with_id(String userId) {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .delete("/api/safety/block/" + userId);
        
        testContext.setLastResponse(response);
    }

    @When("I report user with ID {string} for {string}")
    public void i_report_user_with_id_for(String userId, String reason) {
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("reportedUserId", userId);
        reportData.put("reason", reason);
        reportData.put("description", "Test report description");
        reportData.put("severity", "MEDIUM");
        
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .contentType(ContentType.JSON)
            .body(reportData)
            .when()
            .post("/api/safety/report");
        
        testContext.setLastResponse(response);
    }

    @When("I get my blocked users")
    public void i_get_my_blocked_users() {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .get("/api/safety/blocked-users");
        
        testContext.setLastResponse(response);
    }

    @When("I get my safety blocks")
    public void i_get_my_safety_blocks() {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .get("/api/safety/blocks");
        
        testContext.setLastResponse(response);
    }

    @When("I create a safety block for user {string} with reason {string}")
    public void i_create_a_safety_block_for_user_with_reason(String userId, String reason) {
        Map<String, Object> blockData = new HashMap<>();
        blockData.put("blockedUserId", userId);
        blockData.put("reason", reason);
        blockData.put("blockType", "USER_INITIATED");
        blockData.put("duration", "PERMANENT");
        
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .contentType(ContentType.JSON)
            .body(blockData)
            .when()
            .post("/api/safety/safety-block");
        
        testContext.setLastResponse(response);
    }

    @Then("the user should be blocked successfully")
    public void the_user_should_be_blocked_successfully() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("message", containsString("blocked"));
    }

    @Then("the user should be unblocked successfully")
    public void the_user_should_be_unblocked_successfully() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("message", containsString("unblocked"));
    }

    @Then("the report should be submitted successfully")
    public void the_report_should_be_submitted_successfully() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("reportId", notNullValue());
    }

    @Then("I should see my blocked users list")
    public void i_should_see_my_blocked_users_list() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("blockedUsers", notNullValue());
    }

    @Then("the blocked user should be in the list")
    public void the_blocked_user_should_be_in_the_list() {
        String blockedUserId = (String) testContext.getTestData("blockedUserId");
        testContext.getLastResponse()
            .then()
            .body("blockedUsers.find { it.userId == '" + blockedUserId + "' }", notNullValue());
    }

    @Then("the safety block should be created successfully")
    public void the_safety_block_should_be_created_successfully() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("blockId", notNullValue());
    }
}