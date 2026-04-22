package org.example.tears.Repository;

import org.example.tears.Model.CarServiceRequest;

public interface PaymentGateway {

        String initiate(CarServiceRequest request, boolean initialPayment);

        boolean verify(String transactionId);
    }