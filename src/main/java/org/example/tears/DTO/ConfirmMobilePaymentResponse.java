package org.example.tears.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfirmMobilePaymentResponse {

    private Integer orderId;

    private Integer paymentAttemptId;

    private String paymentId;

    private String paymentStatus;

    private String orderStatus;

}