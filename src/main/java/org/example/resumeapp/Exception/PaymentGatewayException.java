package org.example.resumeapp.Exception;

public class PaymentGatewayException extends RuntimeException {
    public PaymentGatewayException(String message) {
        super(message);
    }
}