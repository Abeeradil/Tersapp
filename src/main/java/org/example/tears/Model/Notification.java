package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter@Getter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "is_read", nullable = false)
    private boolean readStatus = false;  // استخدم هذا فقط

    private String message;
    private LocalDateTime createdAt;

    // ربط Notification بالمستخدم
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

}
