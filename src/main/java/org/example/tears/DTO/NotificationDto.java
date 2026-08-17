package org.example.tears.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tears.Enums.NotificationCategory;
import org.example.tears.Enums.NotificationType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private Integer id;

    private NotificationType type;
    private NotificationCategory category;

    private String title;

    private String body;

    private LocalDateTime createdAt;

    private boolean readStatus = false;

    private LocalDateTime readAt;

    private NotificationActionDto action;
}