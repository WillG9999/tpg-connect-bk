package com.tpg.connect.cucumber.steps;

import com.tpg.connect.cucumber.TestContext;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class NotificationSteps {

    @Autowired
    private TestContext testContext;

    @When("I get my notifications")
    public void i_get_my_notifications() {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .get("/api/notifications");
        
        testContext.setLastResponse(response);
    }

    @When("I get my unread notifications count")
    public void i_get_my_unread_notifications_count() {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .get("/api/notifications/unread/count");
        
        testContext.setLastResponse(response);
    }

    @When("I mark notification {string} as read")
    public void i_mark_notification_as_read(String notificationId) {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .put("/api/notifications/" + notificationId + "/read");
        
        testContext.setLastResponse(response);
    }

    @When("I mark all notifications as read")
    public void i_mark_all_notifications_as_read() {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .put("/api/notifications/read-all");
        
        testContext.setLastResponse(response);
    }

    @When("I delete notification {string}")
    public void i_delete_notification(String notificationId) {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .delete("/api/notifications/" + notificationId);
        
        testContext.setLastResponse(response);
    }

    @When("I delete all my notifications")
    public void i_delete_all_my_notifications() {
        Response response = given()
            .header("Authorization", "Bearer " + testContext.getAccessToken())
            .when()
            .delete("/api/notifications");
        
        testContext.setLastResponse(response);
    }

    @Then("I should see my notifications list")
    public void i_should_see_my_notifications_list() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("notifications", notNullValue());
    }

    @Then("the response should contain unread count")
    public void the_response_should_contain_unread_count() {
        testContext.getLastResponse()
            .then()
            .body("unreadCount", notNullValue());
    }

    @Then("the notification should be marked as read")
    public void the_notification_should_be_marked_as_read() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true));
    }

    @Then("all notifications should be marked as read")
    public void all_notifications_should_be_marked_as_read() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("message", containsString("marked as read"));
    }

    @Then("the notification should be deleted successfully")
    public void the_notification_should_be_deleted_successfully() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("message", containsString("deleted"));
    }

    @Then("all notifications should be deleted successfully")
    public void all_notifications_should_be_deleted_successfully() {
        testContext.getLastResponse()
            .then()
            .statusCode(200)
            .body("success", equalTo(true))
            .body("message", containsString("deleted"));
    }
}