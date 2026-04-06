package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.Data;
import org.example.tears.Enums.AssignmentStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Entity
@Data
public class RequestAssignment {

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne
    private CarServiceRequest request;

    @ManyToOne
    private User employee;

    private LocalDateTime assignedAt;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;
}
