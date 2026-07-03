package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.Data;
import org.example.tears.Enums.StaffRequestStatus;

import java.time.LocalDateTime;
@Entity
@Data
public class RequestNote {

        @Id
        @GeneratedValue
        private Integer id;

        private String note;

        @ManyToOne
        @JoinColumn(name = "employee_id")
        private Employee employee;

        @Enumerated(EnumType.STRING)
        private StaffRequestStatus step;

        private LocalDateTime createdAt;

        @ManyToOne
        @JoinColumn(name = "request_id")
        private CarServiceRequest request;
}