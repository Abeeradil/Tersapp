package org.example.tears.DTO;

import lombok.Data;

@Data
public class RegisterDeviceDto {

    private String fcmToken;

    private String deviceType;
}
