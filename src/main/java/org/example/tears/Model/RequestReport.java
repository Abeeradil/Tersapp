package org.example.tears.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class RequestReport {

        @Id
        @GeneratedValue
        private Integer id;

        private String fileUrl;

        private String description;

        private LocalDateTime createdAt;

        @ManyToOne
        private CarServiceRequest request;
    }
