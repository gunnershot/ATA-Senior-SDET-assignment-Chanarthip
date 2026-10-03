package com.assignment.models;

/**
 * Catalogue items used by the tests. {@code slug} is the suffix SauceDemo uses in its
 * {@code data-test} ids, e.g. {@code add-to-cart-sauce-labs-backpack}.
 */
public enum Product {
    BACKPACK("Sauce Labs Backpack", "sauce-labs-backpack"),
    BIKE_LIGHT("Sauce Labs Bike Light", "sauce-labs-bike-light");

    private final String displayName;
    private final String slug;

    Product(String displayName, String slug) {
        this.displayName = displayName;
        this.slug = slug;
    }

    public String displayName() {
        return displayName;
    }

    public String slug() {
        return slug;
    }
}

