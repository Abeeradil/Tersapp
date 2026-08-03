package org.example.tears.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfirmFinalMobilePaymentResponse {

    private Integer requestId;
    private Integer paymentIntentId;
    private String paymentId;
    private String paymentStatus;
    private String requestStatus;
}