package org.example.tears.Repository;

import org.example.tears.Model.PaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Integer> {
    Optional<PaymentIntent> findByPaymentId(String paymentId);
}
