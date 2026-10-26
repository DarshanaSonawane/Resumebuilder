package org.example.resumeapp.Repository;

import jakarta.persistence.LockModeType;
import org.example.resumeapp.Entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {

    // Row lock: /verify and the webhook can arrive at the same moment for the same order.
    // Must be called inside a transaction.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PaymentEntity> findByRazorpayOrderId(String razorpayOrderId);

    List<PaymentEntity> findByUserIdAndStatusInOrderByCreatedAtDesc(
            String userId, Collection<PaymentEntity.Status> statuses);
}