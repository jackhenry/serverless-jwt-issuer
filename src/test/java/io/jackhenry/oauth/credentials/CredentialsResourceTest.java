package io.jackhenry.oauth.credentials;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.vertx.core.json.JsonObject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import javax.inject.Inject;

@QuarkusTest
public class CredentialsResourceTest {
  @ConfigProperty(name = "default.admin.clientid")
  String adminClientId;

  @ConfigProperty(name = "default.admin.secret")
  String adminSecret;

  @Inject
  JWTParser parser;

  String clientid;
  String secret;

  @BeforeEach
  public void createNonAdminUser() {
    JsonObject params = new JsonObject();
    params.put("id", adminClientId);
    params.put("secret", adminSecret);
    var response = given()
        .body(params.encodePrettily())
        .when()
        .post("/admin/client/create")
        .thenReturn();

    JsonObject responseJson = new JsonObject(response.asPrettyString());
    JsonObject data = responseJson.getJsonObject("data");
    clientid = data.getString("id");
    secret = data.getString("secret");
  }

  @Test
  public void testIssueValidCredentials() {
    JsonObject params = new JsonObject();
    params.put("id", clientid);
    params.put("secret", secret);

    given()
        .body(params.encode())
        .when()
        .post("/credentials/issue")
        .then()
        .body("status", equalTo("success"),
            "data.expiresIn", isA(int.class),
            "data.token", notNullValue(),
            "data.refresh", notNullValue());
  }

  @Test
  public void testIssueInvalidSecret() {
    JsonObject params = new JsonObject();
    params.put("id", clientid);
    params.put("secret", secret + "not valid");

    given()
        .body(params.encode())
        .when()
        .post("/credentials/issue")
        .then()
        .body("status", equalTo("fail"),
            "data.message", equalTo("Invalid credentials"));
  }

  @Test
  public void testIssueInvalidCredentials() {
    JsonObject params = new JsonObject();
    params.put("id", clientid + "not valid");
    params.put("secret", secret + "not valid");

    given()
        .body(params.encode())
        .when()
        .post("/credentials/issue")
        .then()
        .body("status", equalTo("fail"),
            "data.message", equalTo("Invalid credentials"));
  }

  @Test
  public void testIssueAccessTokenContent() throws ParseException {
    JsonObject params = new JsonObject();
    params.put("id", clientid);
    params.put("secret", secret);

    var response = given()
        .body(params.encode())
        .when()
        .post("/credentials/issue")
        .thenReturn();

    JsonObject responseJson = new JsonObject(response.asString());
    var data = responseJson.getJsonObject("data");
    String accessTokenString = data.getString("token");
    testToken(accessTokenString, "access");
  }

  @Test
  public void testIssueRefreshTokenContent() throws ParseException {
    JsonObject params = new JsonObject();
    params.put("id", clientid);
    params.put("secret", secret);
    var response = given()
        .body(params.encode())
        .when()
        .post("/credentials/issue")
        .thenReturn();

    JsonObject responseJson = new JsonObject(response.asString());
    var data = responseJson.getJsonObject("data");
    String refreshTokenString = data.getString("refresh");
    testToken(refreshTokenString, "refresh");
  }

  @Test
  public void testRefresh() throws ParseException {
    JsonObject params = new JsonObject();
    params.put("id", clientid);
    params.put("secret", secret);
    var response = given()
        .body(params.encode())
        .when()
        .post("/credentials/issue")
        .thenReturn();

    JsonObject json = new JsonObject(response.asString());
    var jsonData = json.getJsonObject("data");
    String refreshToken = jsonData.getString("refresh");

    JsonObject refreshParams = new JsonObject();
    refreshParams.put("token", refreshToken);
    var refreshResponse = given()
        .body(refreshParams.encode())
        .when()
        .post("/credentials/refresh")
        .then()
        .body("status", equalTo("success"),
            "data.expiresIn", isA(int.class),
            "data.token", notNullValue(),
            "data.refresh", notNullValue())
        .extract();

    // Parse the token
    JsonObject responseJson = new JsonObject(refreshResponse.asString());
    var data = responseJson.getJsonObject("data");
    String accessTokenString = data.getString("token");
    String refreshTokenString = data.getString("refresh");
    testToken(accessTokenString, "access");
    testToken(refreshTokenString, "refresh");
  }

  @Test
  public void testInvalidRefreshToken() throws ParseException {
    JsonObject params = new JsonObject();
    params.put("id", clientid);
    params.put("secret", secret);
    var response = given()
        .body(params.encode())
        .when()
        .post("/credentials/issue")
        .thenReturn();

    JsonObject responseJson = new JsonObject(response.asString());
    var data = responseJson.getJsonObject("data");
    String refreshToken = data.getString("token");

    JsonObject refreshParams = new JsonObject();
    refreshParams.put("token", refreshToken);
    given()
        .body(refreshParams.encode())
        .when()
        .post("/credentials/refresh")
        .then()
        .body("status", equalTo("fail"),
            "data.message", equalTo("Invalid token"));
  }

  private void testToken(String tokenString, String tokenType) throws ParseException {
    var token = parser.parse(tokenString);
    var upn = (String) token.getClaim("upn");
    assertEquals(upn, clientid);
    // Type should be refresh
    var type = (String) token.getClaim("type");
    assertEquals(tokenType, type);
    // Ensure that the expiration time comes after current time
    var expiration = token.getExpirationTime();
    var expirationDate = new Date(expiration * 1000);
    var currentDate = new Date();
    assertTrue(expirationDate.after(currentDate));
  }
}
