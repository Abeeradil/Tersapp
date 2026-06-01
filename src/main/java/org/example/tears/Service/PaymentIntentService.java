package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Enums.PaymentStatus;
import org.example.tears.Enums.ServiceOption;
import org.example.tears.InpDTO.CreateRequestStepDto;
import org.example.tears.Model.PaymentIntent;
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

@Service
@RequiredArgsConstructor
public class PaymentIntentService {

    private final PaymentIntentRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${MOYASAR_SECRET_KEY}")
    private String secretKey;

    @Value("${MOYASAR_CALLBACK_URL}")
    private String callbackUrl;

    public Map<String, String> createPaymentIntent(CreateRequestStepDto dto) {

        PaymentIntent intent = new PaymentIntent();

        intent.setCarId(dto.getCarId());
        intent.setServiceOption(ServiceOption.valueOf(dto.getServiceOption().toUpperCase()));
        intent.setProblemDescription(dto.getProblemDescription());
        intent.setAppointmentDate(dto.getAppointmentDate());
        intent.setAppointmentTime(dto.getAppointmentTime());
        intent.setHydraulicTruck(dto.isHydraulicTruck());
        intent.setCouponCode(dto.getCouponCode());

        int price = intent.getServiceOption().getPrice();
        if (Boolean.TRUE.equals(intent.getHydraulicTruck())) {
            price += 100;
        }

        intent.setEstimatedPrice(price);
        intent.setPaymentStatus(PaymentStatus.PENDING);
        intent.setCreatedAt(LocalDateTime.now());

        repository.save(intent);

        // Moyasar
        int amount = price * 100;

        Map<String, Object> source = new HashMap<>();
        source.put("type", "creditcard");

        Map<String, Object> body = new HashMap<>();
        body.put("amount", amount);
        body.put("currency", "SAR");
        body.put("description", "PaymentIntent #" + intent.getId());
        body.put("callback_url", callbackUrl);
        body.put("source", source);

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(secretKey, "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.moyasar.com/v1/payments",
                entity,
                Map.class
        );

        Map data = response.getBody();

        if (data == null || data.get("id") == null) {
            throw new RuntimeException("Moyasar failed: " + data);
        }

        String checkoutUrl = null;

        if (data.get("redirect_url") != null) {
            checkoutUrl = data.get("redirect_url").toString();
        } else if (data.get("source") instanceof Map sourceMap) {
            Object tx = sourceMap.get("transaction_url");
            if (tx != null) {
                checkoutUrl = tx.toString();
            }
        }

        intent.setPaymentId(intent.getPaymentId());
        intent.setCheckoutUrl(checkoutUrl);
        intent.setPaymentStatus(PaymentStatus.INITIATED);

        repository.save(intent);

        Map<String, String> result = new HashMap<>();
        result.put("paymentIntentId", intent.getId().toString());
        result.put("checkoutUrl", checkoutUrl);

        return result;
    }
}