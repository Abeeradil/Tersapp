package org.example.tears.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfirmMobilePaymentResponse {

        private Integer requestId;
        private Integer paymentIntentId;
        private String requestStatus;
        private String serviceOption;
        private String location;
        private String appointmentDate;
        private String carInfo;
        private Double totalAmount;
        private Integer paymentAttemptId;
        private String paymentId;
        private String paymentStatus;

}