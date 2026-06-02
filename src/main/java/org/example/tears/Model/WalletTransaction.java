package org.example.tears.Model;

import jakarta.persistence.*;
import org.example.tears.Enums.TransactionType;

import java.time.LocalDateTime;

@Entity
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Wallet wallet;

    private Integer amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private String referenceNumber;

    private String description;

    private LocalDateTime createdAt;
}