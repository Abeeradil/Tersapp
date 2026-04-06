package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Entity
@Data
public class RequestNote {

        @Id
        @GeneratedValue
        private Integer id;

        private String note;

        private Integer employeeId;

        private LocalDateTime createdAt;

        @ManyToOne
        @JoinColumn(name = "request_id")
        private CarServiceRequest request;
    }

