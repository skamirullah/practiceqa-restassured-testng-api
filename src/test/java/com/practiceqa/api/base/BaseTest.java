package com.practiceqa.api.base;

import com.practiceqa.api.client.AuthClient;
import com.practiceqa.api.client.ProductsClient;
import com.practiceqa.api.client.UtilityClient;
import com.practiceqa.api.data.User;
import com.practiceqa.api.server.PracticeApiServer;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

/** Starts the API once per run and resets its data before every test, so tests never depend on each other. */
public abstract class BaseTest {
  protected final AuthClient auth = new AuthClient();
  protected final ProductsClient products = new ProductsClient();
  private final UtilityClient utility = new UtilityClient();

  @BeforeSuite(alwaysRun = true)
  public void startApi() {
    PracticeApiServer.start();
  }

  @AfterSuite(alwaysRun = true)
  public void stopApi() {
    PracticeApiServer.stop();
  }

  @BeforeMethod(alwaysRun = true)
  public void resetData() {
    utility.reset().then().statusCode(200);
  }

  protected String adminToken() {
    return auth.tokenFor(User.ADMIN);
  }

  protected String customerToken() {
    return auth.tokenFor(User.CUSTOMER);
  }
}
