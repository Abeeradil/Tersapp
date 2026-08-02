package org.example.tears.DTO;

import lombok.Data;

@Data
public class ConfirmMobilePaymentRequest {

    private Integer paymentAttemptId;

    private String paymentId;

}