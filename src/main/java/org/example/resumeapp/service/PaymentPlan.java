package org.example.resumeapp.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.resumeapp.Exception.InvalidPaymentException;

/**
 * The server decides what each plan costs. The client only sends a plan id,
 * so nobody can pay less by editing the request.
 */
@Getter
@RequiredArgsConstructor
public enum PaymentPlan {

    // 29900 paise = Rs 299.00, one-time payment. Change the price here.
    // subscriptionValue is what gets saved in users.subscription after payment:
    // use whatever string your frontend treats as the paid plan.
    PREMIUM("premium", "Premium", 29900L, "premium");

    private final String id;
    private final String displayName;
    private final long amountPaise;
    private final String subscriptionValue;

    public static PaymentPlan fromId(String id) {
        if (id != null) {
            for (PaymentPlan plan : values()) {
                if (plan.id.equalsIgnoreCase(id.trim())) {
                    return plan;
                }
            }
        }
        throw new InvalidPaymentException("Unknown plan");
    }
}