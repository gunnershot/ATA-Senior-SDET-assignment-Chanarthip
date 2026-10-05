package com.assignment.models;

import java.math.BigDecimal;

/**
 * Catalogue items used by the tests. {@code slug} is the suffix SauceDemo uses in its
 * {@code data-test} ids, e.g. {@code add-to-cart-sauce-labs-backpack}.
 */
public enum Product {
    BACKPACK("Sauce Labs Backpack", "sauce-labs-backpack", new BigDecimal("29.99")),
    BIKE_LIGHT("Sauce Labs Bike Light", "sauce-labs-bike-light", new BigDecimal("9.99")),
    BOLT_T_SHIRT("Sauce Labs Bolt T-Shirt", "sauce-labs-bolt-t-shirt", new BigDecimal("15.99")),
    FLEECE_JACKET("Sauce Labs Fleece Jacket", "sauce-labs-fleece-jacket", new BigDecimal("49.99")),
    ONESIE("Sauce Labs Onesie", "sauce-labs-onesie", new BigDecimal("7.99")),
    TEST_ALL_THE_THINGS_T_SHIRT("Test.allTheThings() T-Shirt (Red)", "test.allthethings()-t-shirt-(red)", new BigDecimal("15.99"));

    private final String displayName;
    private final String slug;
    private final BigDecimal price;

    Product(String displayName, String slug, BigDecimal price) {
        this.displayName = displayName;
        this.slug = slug;
        this.price = price;
    }

    public String displayName() {
        return displayName;
    }

    public String slug() {
        return slug;
    }

    public BigDecimal price() {
        return price;
    }

    public static Product fromSlug(String slug) {
        for (Product product : values()) {
            if (product.slug.equalsIgnoreCase(slug)) {
                return product;
            }
        }
        throw new IllegalArgumentException("Unknown product slug: " + slug);
    }

    public static Product fromDisplayName(String displayName) {
        for (Product product : values()) {
            if (product.displayName.equalsIgnoreCase(displayName)) {
                return product;
            }
        }
        throw new IllegalArgumentException("Unknown product display name: " + displayName);
    }
}
