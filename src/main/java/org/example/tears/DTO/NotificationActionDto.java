package org.example.tears.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tears.Enums.NotificationActionType;
import org.example.tears.Enums.NotificationEntityType;
import org.example.tears.Enums.NotificationSection;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationActionDto {

    private NotificationActionType type;

    private NotificationEntityType entityType;

    private String entityId;

    private NotificationSection section;
}