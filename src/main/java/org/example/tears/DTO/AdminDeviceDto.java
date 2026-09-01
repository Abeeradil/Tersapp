package org.example.tears.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDeviceDto {

    private Integer id;
    private Integer userId;
    private String userName;
    private String userPhone;
    private String fcmToken;
    private String deviceType;
    private Boolean active;
    private LocalDateTime lastSeen;
}