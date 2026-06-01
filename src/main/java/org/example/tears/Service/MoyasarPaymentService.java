package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Enums.CustomerRequestStatus;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

        @Service
        @RequiredArgsConstructor
        public class MoyasarPaymentService {

                private final CarServiceRequestRepository requestRepository;
                private final RestTemplate restTemplate = new RestTemplate();


                @Value("${MOYASAR_SECRET_KEY}")
                private String secretKey;

                @Value("${MOYASAR_CALLBACK_URL}")
                private String callbackUrl;

                public String createPayment(Integer requestId) {

                    CarServiceRequest request = requestRepository.findById(requestId)
                            .orElseThrow(() -> new RuntimeException("Request not found"));

                    int amount = request.getEstimatedPrice() * 100;

                    HttpHeaders headers = new HttpHeaders();
                    headers.setBasicAuth(secretKey, "");
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    Map<String, Object> body = new HashMap<>();
                    body.put("amount", amount);
                    body.put("currency", "SAR");
                    body.put("description", "Request #" + request.getOrderNumber());
                    body.put("callback_url", callbackUrl);

                    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                    ResponseEntity<Map> response = restTemplate.postForEntity(
                            "https://api.moyasar.com/v1/payments",
                            entity,
                            Map.class
                    );

                    Map data = response.getBody();

                    if (data == null || data.get("id") == null) {
                        throw new RuntimeException("Payment creation failed");
                    }

                    String paymentId = (String) data.get("id");

                    request.setInitialTransactionId(paymentId);
                    request.setInitialPaid(false);

                    requestRepository.save(request);

                    return paymentId;
                }


                public void confirmPayment(String paymentId) {

                    CarServiceRequest request =
                            requestRepository.findByInitialTransactionId(paymentId)
                                    .orElseThrow(() -> new RuntimeException("Not found"));

                    request.setInitialPaid(true);
                    request.setCustomerStatus(CustomerRequestStatus.REQUEST_CREATED);

                    requestRepository.save(request);
                }
            }