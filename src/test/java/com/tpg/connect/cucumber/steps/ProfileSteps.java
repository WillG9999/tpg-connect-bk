package com.tpg.connect.cucumber.steps;

import com.tpg.connect.cucumber.TestContext;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ProfileSteps {

    @Autowired
    private TestContext testContext;

    @When("I get my current profile")
    public void i_get_my_current_profile() {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .get("/api/user/profile");
        
        testContext.setLastResponse(response);
    }

    @When("I update my basic info with bio {string}")
    public void i_update_my_basic_info_with_bio(String bio) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("bio", bio);
        updateData.put("location", "Updated Location");
        updateData.put("interests", List.of("Music", "Travel", "Photography"));
        
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .contentType(ContentType.JSON)
            .body(updateData)
            .when()
            .put("/api/user/profile/basic");
        
        testContext.setLastResponse(response);
    }

    @When("I update my basic profile information")
    public void i_update_my_basic_profile_information() {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("location", "Updated Location");
        updateData.put("interests", List.of("Music", "Travel", "Photography"));
        
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .contentType(ContentType.JSON)
            .body(updateData)
            .when()
            .put("/api/user/profile/basic");
        
        testContext.setLastResponse(response);
    }

    @When("I update my profile preferences")
    public void i_update_my_profile_preferences() {
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("ageRangeMin", 25);
        preferences.put("ageRangeMax", 35);
        preferences.put("maxDistance", 50);
        preferences.put("interestedIn", "FEMALE");
        
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .contentType(ContentType.JSON)
            .body(preferences)
            .when()
            .put("/api/user/profile/preferences");
        
        testContext.setLastResponse(response);
    }

    @When("I update my field visibility settings")
    public void i_update_my_field_visibility_settings() {
        Map<String, Object> visibility = new HashMap<>();
        visibility.put("jobTitle", true);
        visibility.put("company", false);
        visibility.put("university", true);
        visibility.put("politics", false);
        
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .contentType(ContentType.JSON)
            .body(visibility)
            .when()
            .put("/api/user/profile/field-visibility");
        
        testContext.setLastResponse(response);
    }

    @When("I add a written prompt with question {string} and answer {string}")
    public void i_add_a_written_prompt_with_question_and_answer(String question, String answer) {
        Map<String, Object> prompt = new HashMap<>();
        prompt.put("prompt", question);
        prompt.put("answer", answer);
        
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .contentType(ContentType.JSON)
            .body(List.of(prompt))
            .when()
            .put("/api/user/profile/written-prompts");
        
        testContext.setLastResponse(response);
    }

    @When("I update my photos order")
    public void i_update_my_photos_order() {
        // Mock photo URLs for testing
        List<String> photoUrls = List.of(
            "https://example.com/photo1.jpg",
            "https://example.com/photo2.jpg"
        );
        
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .contentType(ContentType.JSON)
            .body(photoUrls)
            .when()
            .put("/api/user/profile/photos");
        
        testContext.setLastResponse(response);
    }

    @Then("the profile response should contain bio {string}")
    public void the_profile_response_should_contain_bio(String expectedBio) {
        testContext.getLastResponse()
            .then()
            .body("user.bio", equalTo(expectedBio));
    }

    @Then("the profile should contain basic information")
    public void the_profile_should_contain_basic_information() {
        testContext.getLastResponse()
            .then()
            .body("user.name", notNullValue())
            .body("user.age", notNullValue())
            .body("user.location", notNullValue());
    }

    @Then("the profile should be updated successfully")
    public void the_profile_should_be_updated_successfully() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("message", containsString("updated"));
    }
}