package com.practiceqa.api.client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import java.util.Map;

public class ProductsClient extends BaseClient {
  private static final String PRODUCTS = "/api/products";
  private static final String PRODUCT = "/api/products/{id}";

  @Step("List products {query}")
  public Response list(Map<String, ?> query) {
    return request().queryParams(query).get(PRODUCTS);
  }

  public Response list() {
    return list(Map.of());
  }

  @Step("Get product {id}")
  public Response get(int id) {
    return request().get(PRODUCT, id);
  }

  @Step("Get product {id} if it changed since {etag}")
  public Response getIfChanged(int id, String etag) {
    return request().header("If-None-Match", etag).get(PRODUCT, id);
  }

  @Step("Create product")
  public Response create(String token, Object body) {
    return request(token).body(body).post(PRODUCTS);
  }

  @Step("Replace product {id}")
  public Response replace(String token, int id, Object body) {
    return request(token).body(body).put(PRODUCT, id);
  }

  @Step("Update product {id}")
  public Response update(String token, int id, Object body) {
    return request(token).body(body).patch(PRODUCT, id);
  }

  /** PATCH that only succeeds if the product is still at the version named by {@code etag}. */
  @Step("Update product {id} if it is still at {etag}")
  public Response updateIfMatch(String token, int id, String etag, Object body) {
    return request(token).header("If-Match", etag).body(body).patch(PRODUCT, id);
  }

  @Step("Delete product {id}")
  public Response delete(String token, int id) {
    return request(token).delete(PRODUCT, id);
  }
}
