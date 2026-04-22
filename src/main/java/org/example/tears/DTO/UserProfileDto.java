package org.example.tears.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class UserProfileDto {
    private String fullName;
    private String phoneNumber;
    private String dateOfBirth;
    private boolean notificationsEnabled;
}