package com.practiceqa.api.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import com.practiceqa.api.base.BaseTest;
import com.practiceqa.api.model.Product;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Feature("Browsing the catalogue")
public class CatalogueTests extends BaseTest {
  private static final int CATALOGUE_SIZE = 24;

  @Test(groups = "smoke", description = "The first page has 10 products and reports the total")
  public void listsTheFirstPage() {
    products.list().then()
        .statusCode(200)
        .body("page", equalTo(1))
        .body("limit", equalTo(10))
        .body("total", equalTo(CATALOGUE_SIZE))
        .body("data", hasSize(10))
        .header("X-Total-Count", String.valueOf(CATALOGUE_SIZE));
  }

  @Test(description = "Walking every page returns each product exactly once")
  public void pagingReturnsEveryProductOnce() {
    List<Integer> ids = new ArrayList<>();
    int totalPages;
    int page = 1;
    do {
      Response response = products.list(Map.of("page", page, "limit", 5));
      ids.addAll(response.jsonPath().getList("data.id", Integer.class));
      totalPages = response.jsonPath().getInt("totalPages");
    } while (page++ < totalPages);

    assertThat(ids).hasSize(CATALOGUE_SIZE).doesNotHaveDuplicates();
  }

  @DataProvider
  public Object[][] filters() {
    return new Object[][] {
        {"category", Map.of("category", "books"), (Predicate<Product>) p -> p.category().equals("books")},
        {"in stock", Map.of("inStock", "true"), (Predicate<Product>) p -> p.stock() > 0},
        {"price range", Map.of("minPrice", 500, "maxPrice", 1000), (Predicate<Product>) p -> p.price() >= 500 && p.price() <= 1000},
        {"combined", Map.of("category", "books", "inStock", "true", "maxPrice", 800),
            (Predicate<Product>) p -> p.category().equals("books") && p.stock() > 0 && p.price() <= 800},
    };
  }

  @Test(dataProvider = "filters", description = "Every product returned matches the filter")
  public void filtersReturnOnlyMatchingProducts(String name, Map<String, Object> query, Predicate<Product> matches) {
    Map<String, Object> allPages = new java.util.HashMap<>(query);
    allPages.put("limit", 100);

    List<Product> found = products.list(allPages).then().statusCode(200).extract().jsonPath().getList("data", Product.class);

    assertThat(found).as("products for filter: %s", name).isNotEmpty().allMatch(matches);
  }

  @Test(description = "Search ignores letter case")
  public void searchIsCaseInsensitive() {
    List<String> names = products.list(Map.of("search", "MOUSE")).jsonPath().getList("data.name", String.class);

    assertThat(names).containsExactly("Wireless Mouse");
  }

  @Test(description = "sort=-price lists the most expensive product first")
  public void sortsByPriceDescending() {
    List<Product> sorted = products.list(Map.of("sort", "-price", "limit", 100)).jsonPath().getList("data", Product.class);

    assertThat(sorted).isSortedAccordingTo(Comparator.comparingDouble(Product::price).reversed());
  }

  @Test(description = "An invalid query is rejected and every bad parameter is named")
  public void rejectsInvalidQueryParameters() {
    List<String> fields = products.list(Map.of("limit", 500, "sort", "colour")).then()
        .statusCode(400)
        .extract().jsonPath().getList("error.details.field", String.class);

    assertThat(fields).containsExactlyInAnyOrder("limit", "sort");
  }
}
