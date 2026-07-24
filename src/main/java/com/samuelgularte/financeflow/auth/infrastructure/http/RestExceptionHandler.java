package com.samuelgularte.financeflow.auth.infrastructure.http;

import com.samuelgularte.financeflow.auth.application.usecase.response.MessageResponse;
import com.samuelgularte.financeflow.auth.domain.exception.EmailAlreadyRegisteredException;
import com.samuelgularte.financeflow.auth.domain.exception.EmailSendException;
import com.samuelgularte.financeflow.auth.domain.exception.InvalidCredentialsException;
import com.samuelgularte.financeflow.auth.domain.exception.InvalidRefreshTokenException;
import com.samuelgularte.financeflow.auth.domain.exception.UsernameAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<MessageResponse> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        MessageResponse message = new MessageResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<MessageResponse> handleEmailAlreadyRegistered(EmailAlreadyRegisteredException ex) {
        MessageResponse message = new MessageResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<MessageResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        MessageResponse message = new MessageResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(message);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<MessageResponse> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        MessageResponse message = new MessageResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(message);
    }

    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<MessageResponse> handleEmailSend(EmailSendException ex) {
        MessageResponse message = new MessageResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponse> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        MessageResponse message = new MessageResponse(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        MessageResponse message = new MessageResponse("An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }
}
