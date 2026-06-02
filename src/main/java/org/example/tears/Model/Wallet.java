package org.example.tears.Model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    private Customer customer;

    private Integer balance; // بالهللات

    private LocalDateTime createdAt;
}