package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.tears.Enums.TicketPriority;
import org.example.tears.Enums.TicketProblemType;
import org.example.tears.Enums.TicketStatus;

import java.time.LocalDateTime;

@Entity
@Data
@Setter
@Getter
public class Ticket {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @Column(unique = true)
        private String ticketNumber;

        @Enumerated(EnumType.STRING)
        private TicketProblemType problemType;

        @Enumerated(EnumType.STRING)
        private TicketPriority priority;

        @Enumerated(EnumType.STRING)
        private TicketStatus status;

        @Column(columnDefinition = "TEXT")
        private String description;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;

        private LocalDateTime solvedAt;

        @ManyToOne
        @JoinColumn(name = "request_id")
        private CarServiceRequest request;

        @ManyToOne
        @JoinColumn(name = "customer_id")
        private Customer customer;

}