package org.example.tears.Repository;

import org.example.tears.Model.PaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Integer> {
}
