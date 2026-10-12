package com.practiceqa.api.client;

import static io.restassured.RestAssured.given;

import com.practiceqa.api.config.Config;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Builds every request the same way: base URL, JSON, Allure attachment, and the full
 * request and response logged only when an assertion fails.
 */
abstract class BaseClient {
  private static final RequestSpecification SPEC = new RequestSpecBuilder()
      .setBaseUri(Config.baseUrl())
      .setContentType(ContentType.JSON)
      .setAccept(ContentType.JSON)
      .addFilter(new AllureRestAssured())
      .build();

  /** A request with no credentials. */
  protected RequestSpecification request() {
    return given().spec(SPEC).log().ifValidationFails(LogDetail.ALL);
  }

  /** A request carrying a Bearer token; a null token sends no Authorization header. */
  protected RequestSpecification request(String token) {
    RequestSpecification request = request();
    return token == null ? request : request.auth().oauth2(token);
  }
}
