package com.assignment.models;

import java.math.BigDecimal;

/**
 * Catalogue items used by the tests. {@code slug} is the suffix SauceDemo uses in its
 * {@code data-test} ids, e.g. {@code add-to-cart-sauce-labs-backpack}.
 */
public enum Product {
    BACKPACK(
            "Sauce Labs Backpack",
            "sauce-labs-backpack",
            new BigDecimal("29.99"),
            "carry.allTheThings() with the sleek, streamlined Sly Pack that melds uncompromising style with unequaled laptop and tablet protection."
    ),
    BIKE_LIGHT(
            "Sauce Labs Bike Light",
            "sauce-labs-bike-light",
            new BigDecimal("9.99"),
            "A red light isn't the desired state in testing but it sure helps when riding your bike at night. Water-resistant with 3 lighting modes, 1 AAA battery included."
    ),
    BOLT_T_SHIRT(
            "Sauce Labs Bolt T-Shirt",
            "sauce-labs-bolt-t-shirt",
            new BigDecimal("15.99"),
            "Get your testing superhero on with the Sauce Labs bolt T-shirt. From American Apparel, 100% ringspun combed cotton, heather gray with red bolt."
    ),
    FLEECE_JACKET(
            "Sauce Labs Fleece Jacket",
            "sauce-labs-fleece-jacket",
            new BigDecimal("49.99"),
            "It's not every day that you come across a midweight quarter-zip fleece jacket capable of handling everything from a relaxing day outdoors to a busy day at the office."
    ),
    ONESIE(
            "Sauce Labs Onesie",
            "sauce-labs-onesie",
            new BigDecimal("7.99"),
            "Rib snap infant onesie for the junior automation engineer in development. Reinforced 3-snap bottom closure, two-needle hemmed sleeved and bottom won't unravel."
    ),
    TEST_ALL_THE_THINGS_T_SHIRT(
            "Test.allTheThings() T-Shirt (Red)",
            "test.allthethings()-t-shirt-(red)",
            new BigDecimal("15.99"),
            "This classic Sauce Labs t-shirt is perfect to wear when cozying up to your keyboard to automate a few tests. Super-soft and comfy ringspun combed cotton."
    );

    private final String displayName;
    private final String slug;
    private final BigDecimal price;
    private final String description;

    Product(String displayName, String slug, BigDecimal price, String description) {
        this.displayName = displayName;
        this.slug = slug;
        this.price = price;
        this.description = description;
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

    public String description() {
        return description;
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
