package org.example.resumeapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.resumeapp.DTO.PaymentDtos.CreateOrderRequest;
import org.example.resumeapp.DTO.PaymentDtos.CreateOrderResponse;
import org.example.resumeapp.DTO.PaymentDtos.PaymentResponse;
import org.example.resumeapp.DTO.PaymentDtos.PlanResponse;
import org.example.resumeapp.DTO.PaymentDtos.VerifyPaymentRequest;
import org.example.resumeapp.Entity.PaymentEntity;
import org.example.resumeapp.Entity.UserEntity;
import org.example.resumeapp.Exception.InvalidPaymentException;
import org.example.resumeapp.Exception.ResourceNotFoundException;
import org.example.resumeapp.Repository.PaymentRepository;
import org.example.resumeapp.Repository.UserRepository;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String CURRENCY = "INR";

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final RazorpayGateway razorpayGateway;

    // ---------- plans ----------

    public List<PlanResponse> getPlans() {
        return Arrays.stream(PaymentPlan.values())
                .map(plan -> PlanResponse.builder()
                        .id(plan.getId())
                        .name(plan.getDisplayName())
                        .amountPaise(plan.getAmountPaise())
                        .currency(CURRENCY)
                        .build())
                .toList();
    }

    // ---------- step 1: create the order ----------

    // Deliberately NOT @Transactional: the call to Razorpay must not hold a database connection
    public CreateOrderResponse createOrder(String email, CreateOrderRequest request) {
        log.info("Inside PaymentService: createOrder() for {}", email);

        PaymentPlan plan = PaymentPlan.fromId(request.getPlanId());
        UserEntity user = findUser(email);

        if (plan.getSubscriptionValue().equalsIgnoreCase(user.getSubscription())) {
            throw new InvalidPaymentException("You are already on the " + plan.getDisplayName() + " plan");
        }

        // Razorpay receipts can be at most 40 characters: "rcpt_" + 32 hex = 37
        String receipt = "rcpt_" + UUID.randomUUID().toString().replace("-", "");

        String orderId = razorpayGateway.createOrder(
                plan.getAmountPaise(), CURRENCY, receipt,
                Map.of("userId", user.getId(), "planId", plan.getId()));

        paymentRepository.save(PaymentEntity.builder()
                .userId(user.getId())
                .razorpayOrderId(orderId)
                .receipt(receipt)
                .planId(plan.getId())
                .amountPaise(plan.getAmountPaise())
                .currency(CURRENCY)
                .status(PaymentEntity.Status.CREATED)
                .build());

        return CreateOrderResponse.builder()
                .orderId(orderId)
                .amountPaise(plan.getAmountPaise())
                .currency(CURRENCY)
                .keyId(razorpayGateway.getKeyId())
                .planId(plan.getId())
                .planName(plan.getDisplayName())
                .build();
    }

    // ---------- step 2: verify after Checkout succeeds ----------

    @Transactional
    public Map<String, String> verifyPayment(String email, VerifyPaymentRequest request) {
        log.info("Inside PaymentService: verifyPayment() order {} for {}", request.getRazorpayOrderId(), email);

        UserEntity user = findUser(email);

        // Someone else's order gives the same 404 as a missing one
        PaymentEntity payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .filter(p -> p.getUserId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        boolean valid = razorpayGateway.verifyPaymentSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature());
        if (!valid) {
            log.warn("Payment signature mismatch for order {}", request.getRazorpayOrderId());
            throw new InvalidPaymentException("Payment verification failed");
        }

        markPaid(payment, request.getRazorpayPaymentId());

        return Map.of(
                "message", "Payment verified successfully",
                "subscription", PaymentPlan.fromId(payment.getPlanId()).getSubscriptionValue());
    }

    // ---------- step 3: webhook (safety net if the browser closes after paying) ----------

    @Transactional
    public void handleWebhook(byte[] rawBody, String signature, String eventId) {
        // The signature is checked against the RAW bytes, before any parsing
        if (!razorpayGateway.verifyWebhookSignature(rawBody, signature)) {
            throw new InvalidPaymentException("Invalid webhook signature");
        }

        Map<String, Object> root = parseJson(rawBody);
        String event = asString(root.get("event"));
        Object paymentNode = path(root, "payload", "payment", "entity");
        String orderId = asString(path(paymentNode, "order_id"));
        String paymentId = asString(path(paymentNode, "id"));

        if (event == null || orderId == null) {
            log.info("Webhook {} ignored: no event or order id", eventId);
            return;
        }

        // Unknown order (for example another product on the same Razorpay account):
        // answer 200 so Razorpay does not keep retrying
        PaymentEntity payment = paymentRepository.findByRazorpayOrderId(orderId).orElse(null);
        if (payment == null) {
            log.warn("Webhook {} for unknown order {}", eventId, orderId);
            return;
        }

        switch (event) {
            case "payment.captured", "order.paid" -> {
                Object amount = path(paymentNode, "amount");
                if (amount instanceof Number n && n.longValue() != payment.getAmountPaise()) {
                    log.error("Webhook {} amount mismatch for order {}: expected {}, got {}",
                            eventId, orderId, payment.getAmountPaise(), n.longValue());
                    return;
                }
                markPaid(payment, paymentId);
            }
            case "payment.failed" -> {
                // never downgrade a PAID payment
                if (payment.getStatus() == PaymentEntity.Status.CREATED) {
                    payment.setStatus(PaymentEntity.Status.FAILED);
                    payment.setRazorpayPaymentId(paymentId);
                    paymentRepository.save(payment);
                }
            }
            default -> log.info("Webhook event {} ignored", event);
        }
    }

    // ---------- history ----------

    @Transactional(readOnly = true)
    public List<PaymentResponse> history(String email) {
        UserEntity user = findUser(email);
        // abandoned checkouts (CREATED) are left out
        return paymentRepository.findByUserIdAndStatusInOrderByCreatedAtDesc(
                        user.getId(), List.of(PaymentEntity.Status.PAID, PaymentEntity.Status.FAILED))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ---------- helpers ----------

    // Idempotent: /verify and the webhook can both arrive, the second one does nothing.
    // Runs inside the caller's transaction, with the payment row locked.
    private void markPaid(PaymentEntity payment, String razorpayPaymentId) {
        if (payment.getStatus() == PaymentEntity.Status.PAID) {
            return;
        }

        payment.setStatus(PaymentEntity.Status.PAID);
        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        PaymentPlan plan = PaymentPlan.fromId(payment.getPlanId());
        UserEntity user = userRepository.findById(payment.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setSubscription(plan.getSubscriptionValue());
        userRepository.save(user);

        log.info("Payment {} marked PAID, user {} upgraded to {}",
                payment.getId(), user.getId(), plan.getSubscriptionValue());
    }

    private UserEntity findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private PaymentResponse toResponse(PaymentEntity payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .planId(payment.getPlanId())
                .amountPaise(payment.getAmountPaise())
                .currency(payment.getCurrency())
                .status(payment.getStatus().name())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .createdAt(payment.getCreatedAt())
                .paidAt(payment.getPaidAt())
                .build();
    }

    // Spring Boot's own JSON parser: gives plain Maps and Lists, no Jackson types needed
    private Map<String, Object> parseJson(byte[] rawBody) {
        try {
            return JsonParserFactory.getJsonParser().parseMap(new String(rawBody, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new InvalidPaymentException("Invalid webhook payload");
        }
    }

    // Walks nested maps: path(root, "payload", "payment", "entity")
    private Object path(Object node, String... keys) {
        Object current = node;
        for (String key : keys) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(key);
        }
        return current;
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }
}