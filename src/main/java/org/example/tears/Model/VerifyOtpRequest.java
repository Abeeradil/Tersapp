package org.example.tears.Model;

import lombok.Data;

@Data
public class VerifyOtpRequest {

    private String phoneNumber;
    private String code;
}
