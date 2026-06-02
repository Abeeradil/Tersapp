package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.Data;
import org.example.tears.Enums.PaymentMethod;
import org.example.tears.Enums.TransactionType;

import java.time.LocalDateTime;

@Entity
@Data
public class WalletTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Wallet wallet;

    private Integer amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String referenceNumber;

    private String description;

    private LocalDateTime createdAt;
}