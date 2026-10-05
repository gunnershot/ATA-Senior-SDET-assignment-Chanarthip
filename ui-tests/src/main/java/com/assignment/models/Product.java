package com.assignment.models;

/**
 * Catalogue items used by the tests. {@code slug} is the suffix SauceDemo uses in its
 * {@code data-test} ids, e.g. {@code add-to-cart-sauce-labs-backpack}.
 */
public enum Product {
    BACKPACK("Sauce Labs Backpack", "sauce-labs-backpack"),
    BIKE_LIGHT("Sauce Labs Bike Light", "sauce-labs-bike-light"),
    BOLT_T_SHIRT("Sauce Labs Bolt T-Shirt", "sauce-labs-bolt-t-shirt"),
    FLEECE_JACKET("Sauce Labs Fleece Jacket", "sauce-labs-fleece-jacket"),
    ONESIE("Sauce Labs Onesie", "sauce-labs-onesie"),
    TEST_ALL_THE_THINGS_T_SHIRT("Test.allTheThings() T-Shirt (Red)", "test.allthethings()-t-shirt-(red)");

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
