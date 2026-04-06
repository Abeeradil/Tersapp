package org.example.tears.Model;

import jakarta.persistence.*;
import org.example.tears.Enums.AssignmentRole;

import java.time.LocalDateTime;

@Entity
public class Assignment {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        private CarServiceRequest request;

        @ManyToOne
        private User employee;

        @Enumerated(EnumType.STRING)
        private AssignmentRole role;

        private LocalDateTime assignedAt;

        private boolean active;

}
