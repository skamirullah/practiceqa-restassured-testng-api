package com.practiceqa.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Body for creating or replacing a product. Start from {@link #valid()} and change
 * only the field a test is about, so each test shows exactly what it varies.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductRequest(String sku, String name, String category, Object price, Integer stock) {

  public static ProductRequest valid() {
    return new ProductRequest("EL-150", "Smart Watch", "electronics", 4999, 5);
  }

  public ProductRequest withSku(String sku) {
    return new ProductRequest(sku, name, category, price, stock);
  }

  public ProductRequest withName(String name) {
    return new ProductRequest(sku, name, category, price, stock);
  }

  public ProductRequest withCategory(String category) {
    return new ProductRequest(sku, name, category, price, stock);
  }

  /** Price is an Object so negative tests can send the wrong type, such as "free". */
  public ProductRequest withPrice(Object price) {
    return new ProductRequest(sku, name, category, price, stock);
  }
}
