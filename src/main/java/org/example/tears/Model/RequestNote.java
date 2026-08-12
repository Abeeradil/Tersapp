package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.Data;
import org.example.tears.Enums.RequestNoteType;

import java.time.LocalDateTime;
@Entity
@Data
public class RequestNote {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "request_id", nullable = false)
        private CarServiceRequest request;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "employee_id")
        private Employee employee;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "customer_id")
        private Customer customer;

        @Column(nullable = false, columnDefinition = "TEXT")
        private String note;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private RequestNoteType type;

        @Column(nullable = false)
        private LocalDateTime createdAt;
}