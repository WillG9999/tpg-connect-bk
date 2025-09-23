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

public class MatchSteps {

    @Autowired
    private TestContext testContext;

    @When("I get potential matches")
    public void i_get_potential_matches() {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .get("/api/discovery/potential-matches");
        
        testContext.setLastResponse(response);
    }

    @When("I like a user with ID {string}")
    public void i_like_a_user_with_id(String userId) {
        Map<String, String> actionData = new HashMap<>();
        actionData.put("targetUserId", userId);
        actionData.put("action", "LIKE");
        
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .contentType(ContentType.JSON)
            .body(actionData)
            .when()
            .post("/api/matches/action");
        
        testContext.setLastResponse(response);
        testContext.setTestData("likedUserId", userId);
    }

    @When("I dislike a user with ID {string}")
    public void i_dislike_a_user_with_id(String userId) {
        Map<String, String> actionData = new HashMap<>();
        actionData.put("targetUserId", userId);
        actionData.put("action", "DISLIKE");
        
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .contentType(ContentType.JSON)
            .body(actionData)
            .when()
            .post("/api/matches/action");
        
        testContext.setLastResponse(response);
    }

    @When("I get my matches")
    public void i_get_my_matches() {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .get("/api/matches");
        
        testContext.setLastResponse(response);
    }

    @When("I get messages from match {string}")
    public void i_get_messages_from_match(String matchId) {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .get("/api/matches/" + matchId + "/messages");
        
        testContext.setLastResponse(response);
    }

    @When("I send message {string} to match {string}")
    public void i_send_message_to_match(String message, String matchId) {
        Map<String, String> messageData = new HashMap<>();
        messageData.put("content", message);
        messageData.put("type", "TEXT");
        
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .contentType(ContentType.JSON)
            .body(messageData)
            .when()
            .post("/api/matches/" + matchId + "/messages");
        
        testContext.setLastResponse(response);
    }

    @When("I unmatch with user {string}")
    public void i_unmatch_with_user(String matchId) {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .delete("/api/matches/" + matchId);
        
        testContext.setLastResponse(response);
    }

    @When("I get my daily matches")
    public void i_get_my_daily_matches() {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .get("/api/matches/daily");
        
        testContext.setLastResponse(response);
    }

    @Then("the action should be recorded successfully")
    public void the_action_should_be_recorded_successfully() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true));
    }

    @Then("I should see a list of potential matches")
    public void i_should_see_a_list_of_potential_matches() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("matches", notNullValue());
    }

    @Then("the match should be a mutual match")
    public void the_match_should_be_a_mutual_match() {
        testContext.getLastResponse()
            .then()
            .body("isMutualMatch", equalTo(true));
    }

    @Then("I should see my matches list")
    public void i_should_see_my_matches_list() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("matches", notNullValue());
    }

    @Then("the message should be sent successfully")
    public void the_message_should_be_sent_successfully() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("message", notNullValue());
    }

    @Then("the unmatch should be successful")
    public void the_unmatch_should_be_successful() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true));
    }
}