package io.jackhenry.oauth.admin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import static org.hamcrest.Matchers.*;

@QuarkusTest
public class AdminResourceTest {
  @ConfigProperty(name = "default.admin.clientid")
  String adminClientId;

  @ConfigProperty(name = "default.admin.secret")
  String adminSecret;

  @Test
  @DisplayName("Test non admin client creation with correct credentials.")
  public void testCreateNonAdminClient() {
    JsonObject params = new JsonObject();
    params.put("id", adminClientId);
    params.put("secret", adminSecret);
    given()
        .body(params.encodePrettily())
        .when()
        .post("/admin/client/create")
        .then()
        .statusCode(200)
        .body("status", equalTo("success"),
            "data", notNullValue(),
            "data.id", notNullValue(),
            "data.secret", notNullValue());
  }

  @Test
  @DisplayName("Test non admin client creation with correct client id, but invalid secret.")
  public void testCreateNonAdminClientFailure() {
    JsonObject params = new JsonObject();
    params.put("id", adminClientId);
    params.put("secret", adminSecret + "invalid string");
    given()
        .body(params.encodePrettily())
        .when()
        .post("/admin/client/create")
        .then()
        .statusCode(200)
        .body("status", equalTo("fail"),
            "data.message", equalTo("Invalid credentials"));
  }

  @Test
  @DisplayName("Test non admin client creation with invalid client id and secret")
  public void testCreateNonAdminClientInvalidIDInvalidSecret() {
    JsonObject params = new JsonObject();
    params.put("id", adminClientId + "invalid string");
    params.put("secret", adminSecret + "invalid string");
    given()
        .body(params.encodePrettily())
        .when()
        .post("/admin/client/create")
        .then()
        .statusCode(200)
        .body("status", equalTo("fail"),
            "data.message", equalTo("Invalid credentials"));
  }
}
