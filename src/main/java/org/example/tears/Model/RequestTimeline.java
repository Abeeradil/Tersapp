package org.example.tears.Model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class RequestTimeline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private CarServiceRequest request;

    private String title;

    private String description;

    private LocalDateTime createdAt;
}