package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.*;
import org.example.tears.Enums.TicketPriority;
import org.example.tears.Enums.TicketProblemType;
import org.example.tears.Enums.TicketStatus;

import java.time.LocalDateTime;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @Column(unique = true)
        private String ticketNumber;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private TicketProblemType problemType;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private TicketPriority priority;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private TicketStatus status;

        @Column(nullable = false, columnDefinition = "TEXT")
        private String description;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;

        private LocalDateTime solvedAt;

        private Boolean acceptedByCustomerService = false;

        @ManyToOne
        @JoinColumn(name = "request_id", nullable = false)
        private CarServiceRequest request;

        @ManyToOne
        @JoinColumn(name = "customer_id", nullable = false)
        private Customer customer;

        // موظف خدمة العملاء الذي استلم التذكرة
        @ManyToOne
        @JoinColumn(name = "assigned_employee_id")
        private Employee assignedEmployee;

        @ManyToOne
        @JoinColumn(name = "created_by_employee_id")
        private Employee createdByEmployee;
}