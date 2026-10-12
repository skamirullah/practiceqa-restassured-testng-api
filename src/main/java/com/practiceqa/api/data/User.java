package com.practiceqa.api.data;

/** The accounts seeded in the practice API. All data is fictional. */
public enum User {
  ADMIN("admin", "Admin@123"),
  CUSTOMER("ravi", "Ravi@123");

  private final String username;
  private final String password;

  User(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public String username() {
    return username;
  }

  public String password() {
    return password;
  }
}
