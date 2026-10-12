package com.practiceqa.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** A product as the API returns it. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Product(int id, String sku, String name, String category, double price, int stock, double rating) {}
