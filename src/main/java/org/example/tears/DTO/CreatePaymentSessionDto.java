package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.PaymentMethod;

@Data
public class CreatePaymentSessionDto {

    private Integer requestId;
    private PaymentMethod paymentMethod;
    private boolean initialPayment;

}
