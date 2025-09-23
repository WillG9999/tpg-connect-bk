package com.tpg.connect.cucumber.steps;

import com.tpg.connect.cucumber.TestContext;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class HealthSteps {

    @Autowired
    private TestContext testContext;

    @When("I check the application health")
    public void i_check_the_application_health() {
        Response response = given()
            .when()
            .get("/api/health");
        
        testContext.setLastResponse(response);
    }

    @When("I check the actuator health endpoint")
    public void i_check_the_actuator_health_endpoint() {
        Response response = given()
            .when()
            .get("/actuator/health");
        
        testContext.setLastResponse(response);
    }

    @Then("the application should be healthy")
    public void the_application_should_be_healthy() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }

    @Then("the health status should be {string}")
    public void the_health_status_should_be(String expectedStatus) {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("status", equalTo(expectedStatus));
    }
}