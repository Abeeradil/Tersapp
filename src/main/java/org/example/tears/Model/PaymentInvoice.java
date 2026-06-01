package org.example.tears.Model;

import jakarta.persistence.*;
import org.example.tears.Enums.PaymentStatus;

import java.time.LocalDateTime;

@Entity
public class PaymentInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private CarServiceRequest request;

    private Integer amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String paymentId;

    private String checkoutUrl;

    private String type;
    // INITIAL or FINAL

    private LocalDateTime createdAt;
}