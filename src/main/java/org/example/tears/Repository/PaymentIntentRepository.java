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
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Integer> {


    Optional<PaymentIntent> findByInvoiceId(
            String invoiceId
    );
    Optional<PaymentIntent> findByGivenId(String givenId);

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
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PaymentIntent p WHERE p.id = :id")
    Optional<PaymentIntent> findByIdForUpdate(@Param("id") Integer id);

}
