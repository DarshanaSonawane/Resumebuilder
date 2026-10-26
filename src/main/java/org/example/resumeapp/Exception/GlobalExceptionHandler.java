package org.example.resumeapp.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Validation failed");
        body.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidToken(InvalidTokenException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }


    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidFile(InvalidFileException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(413).body(Map.of("message", "File too large. Maximum size is 5MB"));
    }

    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<Map<String, Object>> handleUploadFailure(ImageUploadException ex) {
        return ResponseEntity.status(502).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid email or password"));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabled(DisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Please verify your email before logging in"));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPassword(InvalidPasswordException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }


    @ExceptionHandler(InvalidTemplateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidTemplate(InvalidTemplateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
    @ExceptionHandler(InvalidPaymentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPayment(InvalidPaymentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(PaymentGatewayException.class)
    public ResponseEntity<Map<String, String>> handleGateway(PaymentGatewayException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", ex.getMessage()));
    }
}
