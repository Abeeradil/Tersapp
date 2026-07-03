package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class RequestReport {


                @Id
                @GeneratedValue(strategy = GenerationType.IDENTITY)
                private Integer id;

                @Column(columnDefinition = "TEXT")
                private String inspectionResult;

                @Column(columnDefinition = "TEXT")
                private String technicianNotes;

                @Column(columnDefinition = "TEXT")
                private String recommendations;

                private LocalDateTime createdAt;

                private Boolean sent = false;

                @OneToOne
                @JoinColumn(name = "request_id")
                private CarServiceRequest request;
        }