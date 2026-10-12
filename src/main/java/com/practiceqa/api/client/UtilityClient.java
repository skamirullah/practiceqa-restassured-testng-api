package com.practiceqa.api.client;

import io.qameta.allure.Step;
import io.restassured.response.Response;

public class UtilityClient extends BaseClient {

  @Step("Reset the API to its starting data")
  public Response reset() {
    return request().post("/api/reset");
  }

  public Response health() {
    return request().get("/api/health");
  }
}
