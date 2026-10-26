package org.example.resumeapp.service;

import lombok.extern.slf4j.Slf4j;
import org.example.resumeapp.Exception.PaymentGatewayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;

/**
 * Talks to Razorpay. Uses Spring's RestClient (no Razorpay SDK), so there are no
 * extra Gradle dependencies to clash with Spring Boot's managed versions.
 */
@Slf4j
@Component
public class RazorpayGateway {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private final RestClient restClient;
    private final String keyId;
    private final String keySecret;
    private final String webhookSecret;

    public RazorpayGateway(@Value("${razorpay.key-id}") String keyId,
                           @Value("${razorpay.key-secret}") String keySecret,
                           @Value("${razorpay.webhook-secret:}") String webhookSecret) {
        this.keyId = keyId;
        this.keySecret = keySecret;
        this.webhookSecret = webhookSecret;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl("https://api.razorpay.com/v1")
                .requestFactory(requestFactory)
                .defaultHeaders(headers -> headers.setBasicAuth(keyId, keySecret))
                .build();
    }

    // Public key id: safe to send to the browser. The secret never leaves this class.
    public String getKeyId() {
        return keyId;
    }

    // Creates an order on Razorpay and returns its id (order_xxx).
    // amountPaise: Rs 1 = 100 paise. receipt: your own reference, max 40 characters.
    public String createOrder(long amountPaise, String currency, String receipt, Map<String, String> notes) {
        Map<String, Object> body = Map.of(
                "amount", amountPaise,
                "currency", currency,
                "receipt", receipt,
                "notes", notes);
        try {
            Map<?, ?> response = restClient.post()
                    .uri("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            Object id = response == null ? null : response.get("id");
            if (id == null) {
                throw new PaymentGatewayException("Could not start payment, please try again");
            }
            return id.toString();
        } catch (RestClientResponseException e) {
            // 401 here = wrong key id / secret
            log.error("Razorpay create order failed: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new PaymentGatewayException("Could not start payment, please try again");
        } catch (RestClientException e) {
            log.error("Razorpay create order failed: {}", e.getMessage());
            throw new PaymentGatewayException("Could not start payment, please try again");
        }
    }

    // generated_signature = hmac_sha256(order_id + "|" + razorpay_payment_id, key_secret)
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        String message = orderId + "|" + paymentId;
        return hmacMatches(message.getBytes(StandardCharsets.UTF_8), keySecret, signature);
    }

    // X-Razorpay-Signature = hmac_sha256(RAW request body, webhook secret)
    public boolean verifyWebhookSignature(byte[] rawBody, String signature) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.error("razorpay.webhook-secret is not configured: rejecting webhook");
            return false;
        }
        return hmacMatches(rawBody, webhookSecret, signature);
    }

    private boolean hmacMatches(byte[] data, String secret, String signature) {
        if (signature == null || signature.isBlank()) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            String expected = HexFormat.of().formatHex(mac.doFinal(data));
            // constant-time comparison, so timing can't leak the signature
            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    signature.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 is not available", e);
        }
    }
}