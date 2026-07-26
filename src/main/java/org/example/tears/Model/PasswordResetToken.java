package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
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