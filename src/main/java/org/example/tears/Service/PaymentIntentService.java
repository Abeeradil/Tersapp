package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
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

    private final PaymentIntentRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();
        private final CarServiceRequestRepository requestRepository;

        @Value("${MOYASAR_SECRET_KEY}")
        private String secretKey;

        @Value("${MOYASAR_CALLBACK_URL}")
        private String callbackUrl;

        // 1️⃣ إنشاء Intent فقط
        public Map<String, String> createIntent(CreateRequestStepDto dto) {

            PaymentIntent intent = new PaymentIntent();

            intent.setCarId(dto.getCarId());
            intent.setServiceOption(ServiceOption.valueOf(dto.getServiceOption().toUpperCase()));
            intent.setProblemDescription(dto.getProblemDescription());
            intent.setAppointmentDate(dto.getAppointmentDate());
            intent.setAppointmentTime(dto.getAppointmentTime());
            intent.setHydraulicTruck(dto.isHydraulicTruck());
            intent.setCouponCode(dto.getCouponCode());

            int price = intent.getServiceOption().getPrice();
            if (Boolean.TRUE.equals(intent.getHydraulicTruck())) price += 100;

            intent.setEstimatedPrice(price);
            intent.setPaymentStatus(PaymentStatus.PENDING);
            intent.setCreatedAt(LocalDateTime.now());

            repository.save(intent);

            return Map.of(
                    "paymentIntentId", intent.getId().toString(),
                    "amount", String.valueOf(price)
            );
        }

        // 2️⃣ إنشاء الدفع (Moyasar)
        public Map<String, String> createCheckout(Integer intentId) {

            PaymentIntent intent = repository.findById(intentId)
                    .orElseThrow(() -> new RuntimeException("Intent not found"));

            int amount = intent.getEstimatedPrice() * 100;

            Map<String, Object> source = new HashMap<>();
            source.put("type", "creditcard");

            Map<String, Object> body = new HashMap<>();
            body.put("amount", amount);
            body.put("currency", "SAR");
            body.put("description", "Intent #" + intent.getId());
            body.put("callback_url", callbackUrl);
            body.put("source", source);

            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(secretKey, "");
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.moyasar.com/v1/payments",
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            Map data = response.getBody();

            String paymentId = data.get("id").toString();

            String checkoutUrl = null;

            if (data.get("source") instanceof Map sourceMap) {
                Object tx = sourceMap.get("transaction_url");
                if (tx != null) checkoutUrl = tx.toString();
            }

            intent.setPaymentId(paymentId);
            intent.setCheckoutUrl(checkoutUrl);
            intent.setPaymentStatus(PaymentStatus.INITIATED);

            repository.save(intent);

            return Map.of(
                    "checkoutUrl", checkoutUrl,
                    "paymentId", paymentId
            );
        }

        // 3️⃣ تأكيد الدفع + إنشاء الطلب
        public void confirmPayment(String paymentId) {

            PaymentIntent intent = repository.findByPaymentId(paymentId)
                    .orElseThrow(() -> new RuntimeException("Not found"));

            if (intent.getPaymentStatus() == PaymentStatus.PAID)
                return;

            intent.setPaymentStatus(PaymentStatus.PAID);
            repository.save(intent);

            // 🔥 إنشاء الطلب الحقيقي هنا
            CarServiceRequest req = new CarServiceRequest();

            req.setCarId(intent.getCarId());
            req.setServiceOption(intent.getServiceOption());
            req.setProblemDescription(intent.getProblemDescription());
            req.setAppointmentDate(intent.getAppointmentDate());
            req.setAppointmentTime(intent.getAppointmentTime());
            req.setHydraulicTruck(intent.getHydraulicTruck());
            req.setEstimatedPrice(intent.getEstimatedPrice());

            req.setCustomerStatus(CustomerRequestStatus.REQUEST_CREATED);
            req.setCreatedAt(LocalDateTime.now());
            req.setOrderNumber("#" + UUID.randomUUID().toString().substring(0, 8));

            requestRepository.save(req);
        }
    }