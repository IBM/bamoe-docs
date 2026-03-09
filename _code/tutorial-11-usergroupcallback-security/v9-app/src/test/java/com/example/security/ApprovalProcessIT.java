package com.example.security;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration test for the approvalProcess BPMN process.
 *
 * Tests the complete flow:
 * 1. Start an approvalProcess instance with a request string
 * 2. Verify a user task is created and waiting
 * 3. Claim and complete the user task as john (who has approver group)
 * 4. Verify the process completes with approved = true
 *
 * Run with: mvn verify (requires the app to be packaged first)
 */
@QuarkusIntegrationTest
public class ApprovalProcessIT {

    @Test
    public void testApprovalProcessWithUserTask() {
        // Step 1: Start the process
        Response startResponse = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"request\": \"Purchase order for $500\"}")
        .when()
            .post("/approvalProcess")
        .then()
            .statusCode(201)
            .body("request", equalTo("Purchase order for $500"))
            .extract().response();

        String processInstanceId = startResponse.path("id");

        // Step 2: Get the user task (should be waiting for approval)
        Response taskResponse = given()
            .queryParam("user", "john")
            .queryParam("group", "approver,manager")
        .when()
            .get("/usertasks/instance")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .extract().response();

        // Find the task for our process instance
        String taskId = taskResponse.path("find { it.processInstanceId == '" + processInstanceId + "' }.id");

        // Step 3: Complete the user task with approval
        given()
            .contentType(ContentType.JSON)
            .queryParam("user", "john")
            .queryParam("group", "approver,manager")
            .body("{\"approved\": true, \"approverComments\": \"Approved by integration test\"}")
        .when()
            .post("/usertasks/instance/" + taskId + "/transition?phase=complete")
        .then()
            .statusCode(200);

        // Step 4: Verify the process completed with approved = true
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/approvalProcess/" + processInstanceId)
        .then()
            .statusCode(200)
            .body("approved", equalTo(true))
            .body("approverComments", equalTo("Approved by integration test"));
    }

    @Test
    public void testApprovalProcessCreatesUserTask() {
        // Start the process
        Response response = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"request\": \"Travel expense reimbursement\"}")
        .when()
            .post("/approvalProcess")
        .then()
            .statusCode(201)
            .body("request", equalTo("Travel expense reimbursement"))
            .extract().response();

        String processInstanceId = response.path("id");

        // Verify a user task exists for this process
        given()
            .queryParam("user", "john")
            .queryParam("group", "approver,manager")
        .when()
            .get("/usertasks/instance")
        .then()
            .statusCode(200)
            .body("find { it.processInstanceId == '" + processInstanceId + "' }.name", equalTo("ApproveRequest"))
            .body("find { it.processInstanceId == '" + processInstanceId + "' }.inputs.request", equalTo("Travel expense reimbursement"));
    }
}
