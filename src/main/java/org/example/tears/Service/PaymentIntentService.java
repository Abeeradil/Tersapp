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

        Map<String, String> result = new HashMap<>();
        result.put("paymentIntentId", intent.getId().toString());
        result.put("amount", String.valueOf(price));

        return result;
    }
}