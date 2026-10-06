package com.assignment.ui;

import com.assignment.base.BaseTest;
import com.assignment.pages.InventoryPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Feature("Product Catalog & Details")
class CatalogTest extends BaseTest {

    @Test
    @Tag("UI-20")
    @Tag("Happy")
    @Tag("P1")
    @Tag("Regression")
    @Story("Catalog Sorting")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("[UI-20] Should sort products descending by name when 'Name (Z to A)' is selected (Ambiguity)")
    void shouldSortProductsDescendingByNameWhenZToAIsSelected() {
        InventoryPage inventory = login();
        
        inventory.sortBy("za"); // Value for 'Name (Z to A)' is usually 'za' on SauceDemo

        List<String> actualNames = inventory.getAllItemNames();
        
        List<String> expectedNames = new ArrayList<>(actualNames);
        expectedNames.sort(Collections.reverseOrder());

        assertEquals(expectedNames, actualNames, "Product names should be sorted in descending order");
        Allure.step("AMBIGUITY: Sorting algorithm (case-sensitive vs insensitive) and default tie-breaker logic are not specified.");
    }
}


