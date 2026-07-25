package com.samuelgularte.financeflow.auth.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenHasherTest {

    @Nested
    @DisplayName("hash")
    class Hash {

        @Test
        @DisplayName("should return 64-character hex string for any input")
        void shouldReturn64CharHex() {
            String hash = TokenHasher.hash("any-token");
            assertEquals(64, hash.length());
            assertTrue(hash.matches("[0-9a-f]{64}"));
        }

        @Test
        @DisplayName("should be deterministic for the same input")
        void shouldBeDeterministic() {
            String hash1 = TokenHasher.hash("my-token");
            String hash2 = TokenHasher.hash("my-token");
            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("should produce different outputs for different inputs")
        void shouldDifferForDifferentInputs() {
            String hash1 = TokenHasher.hash("token-a");
            String hash2 = TokenHasher.hash("token-b");
            assertNotEquals(hash1, hash2);
        }

        @Test
        @DisplayName("should produce different outputs for similar inputs")
        void shouldDifferForSimilarInputs() {
            String hash1 = TokenHasher.hash("token-abc");
            String hash2 = TokenHasher.hash("token-ABc");
            assertNotEquals(hash1, hash2);
        }

        @Test
        @DisplayName("should hash empty string without throwing")
        void shouldHashEmptyString() {
            String hash = TokenHasher.hash("");
            assertEquals(64, hash.length());
            assertTrue(hash.matches("[0-9a-f]{64}"));
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when input is null")
        void shouldThrowWhenNull() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> TokenHasher.hash(null));
            assertEquals("rawToken must not be null", ex.getMessage());
        }
    }
}
