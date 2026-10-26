package org.example.resumeapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.resumeapp.DTO.PaymentDtos.CreateOrderRequest;
import org.example.resumeapp.DTO.PaymentDtos.CreateOrderResponse;
import org.example.resumeapp.DTO.PaymentDtos.PaymentResponse;
import org.example.resumeapp.DTO.PaymentDtos.PlanResponse;
import org.example.resumeapp.DTO.PaymentDtos.VerifyPaymentRequest;
import org.example.resumeapp.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/plans")
    public ResponseEntity<List<PlanResponse>> plans() {
        return ResponseEntity.ok(paymentService.getPlans());
    }

    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("Inside PaymentController: createOrder()");
        return ResponseEntity.ok(paymentService.createOrder(authentication.getName(), request));
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verify(
            Authentication authentication,
            @Valid @RequestBody VerifyPaymentRequest request) {
        log.info("Inside PaymentController: verify()");
        return ResponseEntity.ok(paymentService.verifyPayment(authentication.getName(), request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<PaymentResponse>> history(Authentication authentication) {
        return ResponseEntity.ok(paymentService.history(authentication.getName()));
    }

    // Called by Razorpay, not by your frontend: no JWT, so it must be permitAll() in your
    // security config. It is protected by the HMAC signature check instead.
    // The body is taken as raw bytes because the signature is calculated over the exact bytes.
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody byte[] rawBody,
            @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature,
            @RequestHeader(value = "X-Razorpay-Event-Id", required = false) String eventId) {
        log.info("Inside PaymentController: webhook() eventId {}", eventId);
        paymentService.handleWebhook(rawBody, signature, eventId);
        return ResponseEntity.ok().build();
    }
}