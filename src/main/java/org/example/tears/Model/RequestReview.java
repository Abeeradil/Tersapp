package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data

public class RequestReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    private CarServiceRequest request;

    @ManyToOne
    private Customer customer;

    private Integer rating; // 1 - 5

    @Column(columnDefinition = "TEXT")
    private String comment;

    private LocalDateTime createdAt;
}