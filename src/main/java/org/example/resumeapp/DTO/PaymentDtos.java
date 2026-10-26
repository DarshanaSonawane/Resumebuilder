package org.example.resumeapp.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public final class PaymentDtos {

    private PaymentDtos() {
    }

    // ---------- requests ----------

    @Data
    public static class CreateOrderRequest {
        @NotBlank(message = "Plan is required")
        @Size(max = 32, message = "Plan is too long")
        private String planId;
    }

    // The frontend maps Razorpay Checkout's razorpay_order_id / razorpay_payment_id /
    // razorpay_signature into these camelCase fields
    @Data
    public static class VerifyPaymentRequest {
        @NotBlank(message = "Order id is required")
        @Size(max = 64, message = "Order id is too long")
        private String razorpayOrderId;

        @NotBlank(message = "Payment id is required")
        @Size(max = 64, message = "Payment id is too long")
        private String razorpayPaymentId;

        @NotBlank(message = "Signature is required")
        @Size(max = 128, message = "Signature is too long")
        private String razorpaySignature;
    }

    // ---------- responses ----------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlanResponse {
        private String id;
        private String name;
        private long amountPaise;
        private String currency;
    }

    // Everything Razorpay Checkout needs on the frontend. keyId is the PUBLIC key id;
    // the key secret never leaves the server.
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateOrderResponse {
        private String orderId;
        private long amountPaise;
        private String currency;
        private String keyId;
        private String planId;
        private String planName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentResponse {
        private String id;
        private String planId;
        private long amountPaise;
        private String currency;
        private String status;
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private LocalDateTime createdAt;
        private LocalDateTime paidAt;
    }
}