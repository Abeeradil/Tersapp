package org.example.tears.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationDto {

    private Integer id;
    private String message;
    private Boolean readStatus;
    private LocalDateTime createdAt;
}