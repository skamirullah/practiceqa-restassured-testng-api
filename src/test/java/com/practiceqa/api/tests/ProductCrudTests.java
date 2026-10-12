package com.practiceqa.api.tests;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;

import com.practiceqa.api.base.BaseTest;
import com.practiceqa.api.model.Product;
import com.practiceqa.api.model.ProductRequest;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Feature("Managing products")
public class ProductCrudTests extends BaseTest {
  private static final int EXISTING_ID = 3;
  private static final String EXISTING_SKU = "EL-101";

  @Test(groups = "smoke", description = "An admin creates a product and can read it back from the Location header")
  public void adminCreatesAProduct() {
    ProductRequest request = ProductRequest.valid();

    Response created = products.create(adminToken(), request);

    created.then()
        .statusCode(201)
        .header("Location", matchesPattern("/api/products/\\d+"))
        .body(matchesJsonSchemaInClasspath("schemas/product.json"));
    Product saved = products.get(created.jsonPath().getInt("id")).then().statusCode(200).extract().as(Product.class);
    assertThat(saved.sku()).isEqualTo(request.sku());
    assertThat(saved.name()).isEqualTo(request.name());
  }

  @Test(description = "Creating a product needs a token")
  public void anonymousCannotCreate() {
    products.create(null, ProductRequest.valid()).then().statusCode(401);
  }

  @Test(description = "A customer is not allowed to create a product")
  public void customerCannotCreate() {
    products.create(customerToken(), ProductRequest.valid()).then().statusCode(403);
  }

  @Test(description = "A SKU that already exists is refused")
  public void duplicateSkuIsRefused() {
    products.create(adminToken(), ProductRequest.valid().withSku(EXISTING_SKU)).then().statusCode(409);
  }

  @DataProvider
  public Object[][] invalidProducts() {
    ProductRequest valid = ProductRequest.valid();
    return new Object[][] {
        {"sku", valid.withSku("bad")},
        {"name", valid.withName("x")},
        {"category", valid.withCategory("toys")},
        {"price", valid.withPrice("free")},
        {"price", valid.withPrice(0)},
        {"price", valid.withPrice(-10)},
    };
  }

  @Test(dataProvider = "invalidProducts", description = "An invalid field is rejected and named in the error")
  public void rejectsAnInvalidField(String field, ProductRequest request) {
    List<String> fields = products.create(adminToken(), request).then()
        .statusCode(422)
        .extract().jsonPath().getList("error.details.field", String.class);

    assertThat(fields).containsExactly(field);
  }

  @Test(description = "PATCH changes only the fields that are sent")
  public void patchChangesOnlyTheGivenFields() {
    Product before = products.get(EXISTING_ID).as(Product.class);

    Product after = products.update(adminToken(), EXISTING_ID, Map.of("stock", 7)).then()
        .statusCode(200)
        .extract().as(Product.class);

    assertThat(after.stock()).isEqualTo(7);
    assertThat(after.name()).isEqualTo(before.name());
    assertThat(after.price()).isEqualTo(before.price());
  }

  @Test(description = "PUT replaces the product; fields left out go back to their defaults")
  public void putReplacesTheProduct() {
    ProductRequest replacement = new ProductRequest("EL-103", "USB-C Hub Pro", "electronics", 1499, null);

    products.replace(adminToken(), EXISTING_ID, replacement).then()
        .statusCode(200)
        .body("name", equalTo("USB-C Hub Pro"))
        .body("price", equalTo(1499))
        .body("stock", equalTo(0));
  }

  @Test(description = "A deleted product is gone")
  public void deleteRemovesTheProduct() {
    products.delete(adminToken(), EXISTING_ID).then().statusCode(204);

    products.get(EXISTING_ID).then().statusCode(404);
  }
}
