package org.example.tears.Model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class PasswordResetToken {
        @Id
        @GeneratedValue
        private Integer id;

        private String token;

        private LocalDateTime expiresAt;

        @OneToOne
        private User user;
    }
