package com.samuelgularte.financeflow.budgeting.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    @Test
    @DisplayName("should have exactly 15 constants")
    void shouldHaveFifteenConstants() {
        assertEquals(15, Category.values().length);
    }

    @Test
    @DisplayName("should contain all expected categories")
    void shouldContainAllExpected() {
        assertAll(
                () -> assertNotNull(Category.valueOf("SUPERMARKET")),
                () -> assertNotNull(Category.valueOf("PHARMACY")),
                () -> assertNotNull(Category.valueOf("AUTO")),
                () -> assertNotNull(Category.valueOf("RESTAURANT")),
                () -> assertNotNull(Category.valueOf("UTILITIES")),
                () -> assertNotNull(Category.valueOf("HEALTH")),
                () -> assertNotNull(Category.valueOf("EDUCATION")),
                () -> assertNotNull(Category.valueOf("ENTERTAINMENT")),
                () -> assertNotNull(Category.valueOf("TRANSFER")),
                () -> assertNotNull(Category.valueOf("SUBSCRIPTION")),
                () -> assertNotNull(Category.valueOf("PET")),
                () -> assertNotNull(Category.valueOf("INSURANCE")),
                () -> assertNotNull(Category.valueOf("CLOTHING")),
                () -> assertNotNull(Category.valueOf("HOME")),
                () -> assertNotNull(Category.valueOf("OTHER"))
        );
    }

    @Test
    @DisplayName("should have all unique names")
    void shouldHaveUniqueNames() {
        Set<String> names = new HashSet<>();
        for (var c : Category.values()) {
            assertTrue(names.add(c.name()), "Duplicate name: " + c.name());
        }
    }
}
