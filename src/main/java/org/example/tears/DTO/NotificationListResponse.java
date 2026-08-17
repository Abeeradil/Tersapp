package org.example.tears.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListResponse {

    private List<NotificationDto> items;

    private long unreadCount;

    private boolean hasMore;

    private String nextCursor;
}