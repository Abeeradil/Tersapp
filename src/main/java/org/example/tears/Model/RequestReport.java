package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class RequestReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer version;

    private boolean latest;

    private boolean sent;

    private LocalDateTime createdAt;

    @ManyToOne
    private Employee createdBy;

    @ManyToOne
    private CarServiceRequest request;

        }