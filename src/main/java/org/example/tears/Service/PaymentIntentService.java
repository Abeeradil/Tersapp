package org.example.tears.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.CheckoutResponse;
import org.example.tears.DTO.CreatePaymentIntentRequest;
import org.example.tears.Enums.CustomerRequestStatus;
import org.example.tears.Enums.PaymentMethod;
import org.example.tears.Enums.PaymentStatus;
import org.example.tears.Enums.ServiceOption;
import org.example.tears.InpDTO.CreateRequestStepDto;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.PaymentIntent;
import org.example.tears.Model.User;
import org.example.tears.OutDTO.RequestResponseDto;
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
    private final CarServiceRequestService carServiceRequestService;
    private final AuthService authService;
    private final WalletService walletService;
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
        intent.setHydraulicTruck(dto.getHydraulicTruck());

        int price = intent.getServiceOption().getPrice();
        if (Boolean.TRUE.equals(intent.getHydraulicTruck())) {
            price += 100;
        }

        int amountHalalah = price * 100;

        intent.setEstimatedPrice(price);
        intent.setPaymentStatus(PaymentStatus.INITIATED);
        intent.setCreatedAt(LocalDateTime.now());

        paymentIntentRepository.save(intent);

        Map<String, Object> body = new HashMap<>();
        body.put("amount", amountHalalah);
        body.put("currency", "SAR");
        body.put("description", "Request #" + intent.getId());
        body.put("callback_url", callbackUrl);

        Map<String, Object> source = new HashMap<>();
        source.put("type", "creditcard");

        body.put("source", source);

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(secretKey, "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.moyasar.com/v1/invoices",
                new HttpEntity<>(body, headers),
                Map.class
        );

        Map data = response.getBody();

        String invoiceId = data.get("id").toString();
        String checkoutUrl = data.get("url").toString();

        intent.setInvoiceId(invoiceId);
        intent.setCheckoutUrl(checkoutUrl);

        paymentIntentRepository.save(intent);

        return new CheckoutResponse(
                intent.getId().toString(),
                invoiceId,
                checkoutUrl,
                amountHalalah
        );
    }
    @Transactional
    public RequestResponseDto payRequestWithWallet(
            HttpServletRequest request,
            CreateRequestStepDto dto
    ) {
        User user = authService.getAuthenticatedUser(request);

        CarServiceRequest req =
                carServiceRequestService.buildValidatedRequest(user, dto);

        int amountHalalah = req.getEstimatedPrice() * 100;

        walletService.payFromWallet(
                user,
                amountHalalah,
                "REQ-" + UUID.randomUUID().toString().substring(0, 8)
        );

        req.setPaymentMethod(PaymentMethod.WALLET);

        CarServiceRequest savedRequest = requestRepository.save(req);

        return carServiceRequestService.toResponseDto(savedRequest);
    }

    @Transactional
    public RequestResponseDto handleInvoiceCallback(Map<String, Object> payload) {

        String invoiceId = payload.get("id").toString();
        String status = payload.get("status").toString();

        PaymentIntent intent = paymentIntentRepository.findByInvoiceId(invoiceId)
                .orElseThrow(() -> new ApiException("Payment intent not found"));

        if (!"paid".equalsIgnoreCase(status)) {
            intent.setPaymentStatus(PaymentStatus.FAILED);
            paymentIntentRepository.save(intent);
            throw new ApiException("Payment failed");
        }

        if (intent.getServiceRequest() != null) {
            return carServiceRequestService.toResponseDto(intent.getServiceRequest());
        }

        if (intent.getExpiresAt() != null &&
                intent.getExpiresAt().isBefore(LocalDateTime.now())) {

            intent.setPaymentStatus(PaymentStatus.EXPIRED);
            paymentIntentRepository.save(intent);

            throw new ApiException("Payment intent expired");
        }

        CarServiceRequest req = new CarServiceRequest();

        req.setCustomer(intent.getCustomer());
        req.setCarId(intent.getCarId());
        req.setServiceOption(intent.getServiceOption());
        req.setProblemDescription(intent.getProblemDescription());
        req.setAppointmentDate(intent.getAppointmentDate());
        req.setAppointmentTime(intent.getAppointmentTime());
        req.setHydraulicTruck(intent.getHydraulicTruck());
        req.setEstimatedPrice(intent.getEstimatedPrice());
        req.setPaymentMethod(PaymentMethod.WALLET);
        req.setLocation(intent.getLocation());

        req.setOrderNumber("#" + UUID.randomUUID().toString().substring(0, 8));
        req.setCustomerStatus(CustomerRequestStatus.REQUEST_CREATED);
        req.setCreatedAt(LocalDateTime.now());

        CarServiceRequest savedRequest = requestRepository.save(req);

        intent.setServiceRequest(savedRequest);
        intent.setPaymentStatus(PaymentStatus.PAID);
        intent.setPaidAt(LocalDateTime.now());

        paymentIntentRepository.save(intent);

        return carServiceRequestService.toResponseDto(savedRequest);
    }



    @Transactional
    public RequestResponseDto getPaymentResult(Integer paymentIntentId) {

        PaymentIntent intent = paymentIntentRepository.findById(paymentIntentId)
                .orElseThrow(() -> new ApiException("Payment intent not found"));

        if (intent.getPaymentStatus() != PaymentStatus.PAID) {
            throw new ApiException("Payment is not completed yet");
        }

        if (intent.getServiceRequest() == null) {
            throw new ApiException("Request was not created yet");
        }

        return carServiceRequestService.toResponseDto(intent.getServiceRequest());
    }

}