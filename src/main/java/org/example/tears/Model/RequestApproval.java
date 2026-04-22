package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class RequestApproval {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        // كل طلب له موافقة واحدة
        @OneToOne
        @JoinColumn(name = "request_id", nullable = false)
        private CarServiceRequest request;

        // null = ينتظر, true = موافق, false = مرفوض
        private Boolean approved;

        private LocalDateTime decisionAt;

        @Column(length = 500)
        private String customerNote;
    }
