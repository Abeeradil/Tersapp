package org.example.tears.Config;

import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Repository.PaymentGateway;
import org.springframework.stereotype.Component;

@Component("APPLE_PAY")
public class ApplePayGateway implements PaymentGateway {

    @Override
    public String initiate(CarServiceRequest request, boolean initialPayment) {
        return "APPLE-" + request.getId() + "-" + System.currentTimeMillis();
    }

    @Override
    public boolean verify(String transactionId) {
        return true; // لاحقًا تربط API حقيقي
    }
}