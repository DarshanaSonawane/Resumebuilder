package org.example.resumeapp.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment", indexes = {
        @Index(name = "idx_payment_user_id", columnList = "user_id"),
        @Index(name = "uk_payment_order_id", columnList = "razorpay_order_id", unique = true)
})
public class PaymentEntity {

    public enum Status { CREATED, PAID, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "razorpay_order_id", nullable = false, length = 64)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 64)
    private String razorpayPaymentId;

    @Column(nullable = false, length = 64)
    private String receipt;

    @Column(nullable = false, length = 32)
    private String planId;

    // Razorpay amounts are in paise (Rs 1 = 100 paise)
    @Column(nullable = false)
    private long amountPaise;

    @Column(nullable = false, length = 8)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status;

    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}