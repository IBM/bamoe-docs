package com.example.security;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration test for the userLookup BPMN process.
 *
 * Tests Test 2 from the README:
 * - Start a userLookup process instance with a userId
 * - Verify the process completes and userData is populated with the API response
 *
 * Run with: mvn verify (requires the app to be packaged first)
 *
 * The process calls https://jsonplaceholder.typicode.com/users/{userId}
 * via java.net.http.HttpClient inside the BPMN script task.
 */
@QuarkusIntegrationTest
public class UserLookupProcessIT {

    @Test
    public void testUserLookupWithUserId1() {
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"userId\": 1}")
        .when()
            .post("/userLookup")
        .then()
            .statusCode(201)
            .body("userId", equalTo(1))
            .body("userData", notNullValue())
            .body("userData", containsString("Leanne Graham"));
    }

    @Test
    public void testUserLookupWithUserId2() {
        given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body("{\"userId\": 2}")
        .when()
            .post("/userLookup")
        .then()
            .statusCode(201)
            .body("userId", equalTo(2))
            .body("userData", notNullValue());
    }
}
