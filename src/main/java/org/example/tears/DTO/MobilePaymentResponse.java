package org.example.tears.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MobilePaymentResponse {

    private Integer orderId;

    private Integer paymentAttemptId;

    private String givenId;

    private Integer amount;

    private String currency;

    private String description;

    private String paymentStatus;

}