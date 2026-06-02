package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.CheckoutResponse;
import org.example.tears.DTO.CreatePaymentIntentRequest;
import org.example.tears.Enums.CustomerRequestStatus;
import org.example.tears.Enums.PaymentStatus;
import org.example.tears.Enums.ServiceOption;
import org.example.tears.InpDTO.CreateRequestStepDto;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.PaymentIntent;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.PaymentIntentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentIntentService {

        private final PaymentIntentRepository paymentIntentRepository;
        private final CarServiceRequestRepository requestRepository;
        private final RestTemplate restTemplate = new RestTemplate();

        @Value("${MOYASAR_SECRET_KEY}")
        private String secretKey;

        @Value("${MOYASAR_CALLBACK_URL}")
        private String callbackUrl;

        @Value("${MOYASAR_SUCCESS_URL}")
        private String successUrl;

        @Value("${MOYASAR_BACK_URL}")
        private String backUrl;

        public CheckoutResponse createCheckout(CreatePaymentIntentRequest dto) {
            PaymentIntent intent = new PaymentIntent();

            intent.setCarId(dto.getCarId());
            intent.setServiceOption(ServiceOption.valueOf(dto.getServiceOption().toUpperCase()));
            intent.setProblemDescription(dto.getProblemDescription());
            intent.setAppointmentDate(dto.getAppointmentDate());
            intent.setAppointmentTime(dto.getAppointmentTime());
            intent.setHydraulicTruck(Boolean.TRUE.equals(dto.getHydraulicTruck()));
            intent.setCouponCode(dto.getCouponCode());
            intent.setPaymentStatus(PaymentStatus.PENDING);
            intent.setCreatedAt(LocalDateTime.now());

            int price = intent.getServiceOption().getPrice();

            if (Boolean.TRUE.equals(intent.getHydraulicTruck())) {
                price += 100;
            }

            intent.setEstimatedPrice(price);
            paymentIntentRepository.save(intent);

            int amountInHalalah = price * 100;

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("paymentIntentId", intent.getId().toString());
            metadata.put("carId", intent.getCarId().toString());

            Map<String, Object> body = new HashMap<>();
            body.put("amount", amountInHalalah);
            body.put("currency", "SAR");
            body.put("description", "Car service request #" + intent.getId());
            body.put("callback_url", callbackUrl);
            body.put("success_url", successUrl);
            body.put("back_url", backUrl);
            body.put("metadata", metadata);

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(secretKey, "");
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.moyasar.com/v1/invoices",
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            Map data = response.getBody();

            if (data == null || data.get("id") == null || data.get("url") == null) {
                throw new RuntimeException("Invalid Moyasar invoice response");
            }

            String invoiceId = data.get("id").toString();
            String checkoutUrl = data.get("url").toString();

            intent.setInvoiceId(invoiceId);
            intent.setCheckoutUrl(checkoutUrl);
            intent.setPaymentStatus(PaymentStatus.INITIATED);

            paymentIntentRepository.save(intent);

            return new CheckoutResponse(
                    intent.getId().toString(),
                    invoiceId,
                    checkoutUrl,
                    amountInHalalah
            );
        }

    public void handleInvoiceCallback(Map<String, Object> payload) {
        String invoiceId = payload.get("id").toString();
        String status = payload.get("status").toString();

        PaymentIntent intent = paymentIntentRepository.findByInvoiceId(invoiceId)
                .orElseThrow(() -> new RuntimeException("Payment intent not found"));

        if (!"paid".equalsIgnoreCase(status)) {
            intent.setPaymentStatus(PaymentStatus.FAILED);
            paymentIntentRepository.save(intent);
            return;
        }

        if (intent.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }

        intent.setPaymentStatus(PaymentStatus.PAID);
        paymentIntentRepository.save(intent);

        CarServiceRequest request = new CarServiceRequest();

        request.setCarId(intent.getCarId());
        request.setServiceOption(intent.getServiceOption());
        request.setProblemDescription(intent.getProblemDescription());
        request.setAppointmentDate(intent.getAppointmentDate());
        request.setAppointmentTime(intent.getAppointmentTime());
        request.setHydraulicTruck(intent.getHydraulicTruck());
        request.setEstimatedPrice(intent.getEstimatedPrice());
        request.setCustomerStatus(CustomerRequestStatus.REQUEST_CREATED);
        request.setCreatedAt(LocalDateTime.now());
        request.setOrderNumber("#" + UUID.randomUUID().toString().substring(0, 8));

        requestRepository.save(request);
    }
}