package com.practiceqa.api.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyString;

import com.practiceqa.api.base.BaseTest;
import io.qameta.allure.Feature;
import java.util.Map;
import org.testng.annotations.Test;

@Feature("Caching and optimistic locking")
public class CachingAndLockingTests extends BaseTest {
  private static final int PRODUCT_ID = 1;

  @Test(description = "A product carries an ETag, and asking again with it returns 304 with no body")
  public void unchangedProductIsNotSentAgain() {
    String etag = products.get(PRODUCT_ID).then().statusCode(200).extract().header("ETag");

    products.getIfChanged(PRODUCT_ID, etag).then().statusCode(304).body(emptyString());
  }

  @Test(description = "Every change gives the product a new ETag")
  public void etagChangesWhenTheProductChanges() {
    String before = products.get(PRODUCT_ID).header("ETag");

    String after = products.update(adminToken(), PRODUCT_ID, Map.of("stock", 1)).then().statusCode(200).extract().header("ETag");

    assertThat(after).isNotEqualTo(before);
    products.getIfChanged(PRODUCT_ID, before).then().statusCode(200);
  }

  @Test(description = "An update based on a stale version is refused with 412, so no change is lost")
  public void staleUpdateIsRefused() {
    String admin = adminToken();
    String staleEtag = products.get(PRODUCT_ID).header("ETag");
    products.update(admin, PRODUCT_ID, Map.of("stock", 10)).then().statusCode(200);

    products.updateIfMatch(admin, PRODUCT_ID, staleEtag, Map.of("stock", 99)).then().statusCode(412);

    assertThat(products.get(PRODUCT_ID).jsonPath().getInt("stock")).isEqualTo(10);
  }

  @Test(description = "An update based on the current version goes through")
  public void currentVersionCanBeUpdated() {
    String currentEtag = products.get(PRODUCT_ID).header("ETag");

    products.updateIfMatch(adminToken(), PRODUCT_ID, currentEtag, Map.of("stock", 3)).then().statusCode(200);
  }
}
