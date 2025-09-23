package com.tpg.connect.cucumber.steps;

import com.tpg.connect.cucumber.TestContext;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ConversationSteps {

    @Autowired
    private TestContext testContext;

    @When("I get all my conversations")
    public void i_get_all_my_conversations() {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .get("/api/conversations");
        
        testContext.setLastResponse(response);
    }

    @When("I get conversation {string}")
    public void i_get_conversation(String conversationId) {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .get("/api/conversations/" + conversationId);
        
        testContext.setLastResponse(response);
    }

    @When("I unmatch conversation {string}")
    public void i_unmatch_conversation(String conversationId) {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .delete("/api/conversations/" + conversationId + "/unmatch");
        
        testContext.setLastResponse(response);
    }

    @When("I get conversation summary for {string}")
    public void i_get_conversation_summary_for(String conversationId) {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .get("/api/conversations/" + conversationId + "/summary");
        
        testContext.setLastResponse(response);
    }

    @Then("I should see my conversations list")
    public void i_should_see_my_conversations_list() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("conversations", notNullValue());
    }

    @Then("I should see the conversation details")
    public void i_should_see_the_conversation_details() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("conversation", notNullValue());
    }

    @Then("the conversation should be unmatched successfully")
    public void the_conversation_should_be_unmatched_successfully() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("message", containsString("unmatched"));
    }

    @Then("I should see the conversation summary")
    public void i_should_see_the_conversation_summary() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("summary", notNullValue());
    }
}