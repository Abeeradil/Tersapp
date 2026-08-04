package org.example.tears.Repository;

import org.example.tears.Enums.PaymentIntentType;
import org.example.tears.Enums.PaymentStatus;
import org.example.tears.Model.PaymentIntent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Integer> {


    Optional<PaymentIntent> findByInvoiceId(
            String invoiceId
    );
    Optional<PaymentIntent> findByGivenId(String givenId);

    Optional<PaymentIntent> findByServiceRequestIdAndTypeAndPaymentStatusInAndExpiresAtAfter(
            Integer serviceRequestId,
            PaymentIntentType type,
            List<PaymentStatus> statuses,
            LocalDateTime now
    );

    Optional<PaymentIntent> findByServiceRequestIdAndType(
            Integer serviceRequestId,
            PaymentIntentType type
    );

    boolean existsByAppointmentDateAndAppointmentTimeAndPaymentStatusInAndExpiresAtAfter(
            LocalDate date,
            LocalTime time,
            List<PaymentStatus> statuses,
            LocalDateTime expiresAt
    );

}
