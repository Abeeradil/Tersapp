package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class PasswordResetToken {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @OneToOne
        private User user;

        @Column(unique = true)
        private String token;

        private LocalDateTime expiresAt;

        private Boolean used = false;
}