package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class RequestReport {

        @Id
        @GeneratedValue
        private Integer id;

        private String fileUrl;


        @Column(columnDefinition = "TEXT")
        private String reportContent;


        private LocalDateTime createdAt;

        private boolean sent;

        private String description;

        @ManyToOne
        private CarServiceRequest request;
    }
