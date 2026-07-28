package com.samuelgularte.financeflow.auth.infrastructure.http;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    @DisplayName("should return 404 for EntityNotFoundException")
    void shouldReturnNotFound() {
        var response = handler.handleNotFound(new EntityNotFoundException("Transaction not found"));

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
    }
}
