package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.*;
import org.example.tears.Enums.NotificationActionType;
import org.example.tears.Enums.NotificationCategory;
import org.example.tears.Enums.NotificationEntityType;
import org.example.tears.Enums.NotificationSection;
import org.example.tears.Enums.NotificationType;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "is_read", nullable = false)
    private boolean readStatus = false;

    private LocalDateTime readAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationCategory category;

    @Enumerated(EnumType.STRING)
    private NotificationActionType actionType;

    @Enumerated(EnumType.STRING)
    private NotificationEntityType entityType;

    @Enumerated(EnumType.STRING)
    private NotificationSection section;
}