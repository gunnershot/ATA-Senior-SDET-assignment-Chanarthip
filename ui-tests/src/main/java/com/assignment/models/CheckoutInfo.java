package com.assignment.models;

/** Checkout step-one form data. A {@code null} field is left untouched (not typed into). */
public record CheckoutInfo(String firstName, String lastName, String postalCode) {

    public static CheckoutInfo valid() {
        return new CheckoutInfo("John", "Doe", "10110");
    }

    public CheckoutInfo withFirstName(String value) {
        return new CheckoutInfo(value, lastName, postalCode);
    }

    public CheckoutInfo withLastName(String value) {
        return new CheckoutInfo(firstName, value, postalCode);
    }

    public CheckoutInfo withPostalCode(String value) {
        return new CheckoutInfo(firstName, lastName, value);
    }
}

