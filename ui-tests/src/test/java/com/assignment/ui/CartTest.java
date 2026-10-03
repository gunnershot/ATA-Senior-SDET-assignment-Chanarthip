package com.assignment.ui;

import com.assignment.base.BaseTest;

import com.assignment.models.Product;
import com.assignment.models.User;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import com.assignment.pages.CartPage;
import com.assignment.pages.InventoryPage;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Feature("Shopping Cart Management")
class CartTest extends BaseTest {

    @Test
    @Tag("UI-07")
    @Tag("Happy")
    @Tag("P0")
    @Tag("Smoke")
    @Tag("Regression")
    @Story("Add Item")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("[UI-07] Should update cart badge to 1 and reflect chosen item in cart when adding single item")
    void shouldIncrementBadgeAndDisplayItemInCartWhenSingleItemIsAdded() {
        InventoryPage inventory = loginAs(User.STANDARD).add(Product.BACKPACK);
        assertThat(inventory.cartBadge()).hasText("1");

        CartPage cart = inventory.openCart();
        assertThat(cart.items()).hasCount(1);
        assertThat(cart.itemNames()).hasText(new String[]{Product.BACKPACK.displayName()});
    }

    @Test
    @Tag("UI-08")
    @Tag("Boundary")
    @Tag("P0")
    @Tag("Regression")
    @Story("Add Multiple Items")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("[UI-08] Should update cart badge to 2 and list distinct items without duplication when adding two different products")
    void shouldIncrementBadgeToTwoAndListDistinctItemsWhenAddingTwoDifferentItems() {
        InventoryPage inventory = loginAs(User.STANDARD)
                .add(Product.BACKPACK)
                .add(Product.BIKE_LIGHT);
        assertThat(inventory.cartBadge()).hasText("2");

        CartPage cart = inventory.openCart();
        assertThat(cart.items()).hasCount(2);
        assertThat(cart.itemNames()).hasText(new String[]{
                Product.BACKPACK.displayName(), Product.BIKE_LIGHT.displayName()
        });
    }

    @Test
    @Tag("UI-09")
    @Tag("Happy")
    @Tag("P1")
    @Tag("Regression")
    @Story("Remove Item")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("[UI-09] Should remove item from cart and clear badge count when removing item from inventory page")
    void shouldRemoveItemFromCartAndClearBadgeWhenRemovedFromInventoryPage() {
        InventoryPage inventory = loginAs(User.STANDARD).add(Product.BACKPACK);
        assertThat(inventory.cartBadge()).hasText("1");

        inventory.remove(Product.BACKPACK);
        assertThat(inventory.cartBadge()).isHidden();
        assertThat(inventory.addButton(Product.BACKPACK)).isVisible();

        CartPage cart = inventory.openCart();
        assertThat(cart.items()).hasCount(0);
    }

    @Test
    @Tag("UI-10")
    @Tag("Boundary")
    @Tag("P1")
    @Tag("Regression")
    @Story("Remove Item")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("[UI-10] Should render cart empty and hide cart badge when removing last item directly from cart page")
    void shouldEmptyCartAndHideBadgeWhenRemovingLastItemFromCartPage() {
        CartPage cart = cartWith(User.STANDARD, Product.BACKPACK);
        assertThat(cart.items()).hasCount(1);

        cart.remove(Product.BACKPACK);
        assertThat(cart.items()).hasCount(0);
        assertThat(cart.cartBadge()).isHidden();
    }

    @Test
    @Tag("UI-11")
    @Tag("Edge")
    @Tag("P1")
    @Tag("Regression")
    @Story("Cart State Persistence")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("[UI-11] Should retain added cart items across page refresh without state loss (Assumption A2)")
    void shouldRetainCartStateWhenPageIsRefreshed() {
        InventoryPage inventory = loginAs(User.STANDARD).add(Product.BACKPACK);
        assertThat(inventory.cartBadge()).hasText("1");

        page.reload();

        assertThat(inventory.cartBadge()).hasText("1");
        assertThat(inventory.removeButton(Product.BACKPACK)).isVisible();
    }
}

