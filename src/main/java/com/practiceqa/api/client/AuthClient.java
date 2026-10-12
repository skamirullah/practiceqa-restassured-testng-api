package com.practiceqa.api.client;

import com.practiceqa.api.data.User;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.util.Map;

public class AuthClient extends BaseClient {

  @Step("Log in as {username}")
  public Response login(String username, String password) {
    return request().body(Map.of("username", username, "password", password)).post("/api/auth/login");
  }

  /** Logs in and returns only the token, failing at once if the login does not succeed. */
  public String tokenFor(User user) {
    return login(user.username(), user.password()).then().statusCode(200).extract().path("token");
  }
}
