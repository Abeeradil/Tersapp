package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.CreatePaymentSessionDto;
import org.example.tears.DTO.PaymentResponseDto;
import org.example.tears.Enums.CustomerRequestStatus;
import org.example.tears.Enums.PaymentMethod;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.PaymentGateway;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final CarServiceRequestRepository requestRepository;
    private final Map<String, PaymentGateway> gateways;

    // 🔥 constructor clean
    public PaymentService(CarServiceRequestRepository requestRepository,
                          List<PaymentGateway> gatewayList) {

        this.requestRepository = requestRepository;

        this.gateways = gatewayList.stream()
                .collect(Collectors.toMap(
                        g -> g.getClass().getAnnotation(Component.class).value(),
                        g -> g
                ));
    }

    // =========================
    // INITIATE PAYMENT
    // =========================
    public PaymentResponseDto initiatePayment(CreatePaymentSessionDto dto) {

        CarServiceRequest request = requestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request not found"));

        // 🚨 الدفع فقط قبل الإسناد
        if (request.getCustomerStatus() != CustomerRequestStatus.REQUEST_CREATED) {
            throw new RuntimeException("Payment allowed only before assignment");
        }

        // 🚨 منع الدفع المكرر
        if (Boolean.TRUE.equals(request.getInitialPaid())) {
            throw new RuntimeException("Payment already completed");
        }

        PaymentGateway gateway = gateways.get(dto.getPaymentMethod().name());

        if (gateway == null) {
            throw new RuntimeException("Unsupported payment method");
        }

        String txId = gateway.initiate(request, dto.isInitialPayment());

        request.setPaymentMethod(dto.getPaymentMethod());
        request.setInitialTransactionId(txId);
        request.setInitialPaid(false);

        requestRepository.save(request);

        PaymentResponseDto response = new PaymentResponseDto();
        response.setRequestId(request.getId());
        response.setPaymentMethod(dto.getPaymentMethod());
        response.setPaymentTransactionId(txId);
        response.setPaid(false);
        response.setAmount(request.getEstimatedPrice());

        return response;
    }

    // =========================
    // CONFIRM PAYMENT
    // =========================
    public PaymentResponseDto confirm(String transactionId) {

        CarServiceRequest request = requestRepository.findByInitialTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // 🚨 منع إعادة التأكيد
        if (Boolean.TRUE.equals(request.getInitialPaid())) {
            throw new RuntimeException("Payment already confirmed");
        }

        PaymentGateway gateway = gateways.get(request.getPaymentMethod().name());

        if (gateway == null || !gateway.verify(transactionId)) {
            throw new RuntimeException("Payment verification failed");
        }

        // ✅ تحديث الدفع
        request.setInitialPaid(true);

        // 🔥 أهم نقطة: حالة جديدة أنظف
        request.setCustomerStatus(CustomerRequestStatus.CAR_RECEIVED);

        requestRepository.save(request);

        PaymentResponseDto response = new PaymentResponseDto();
        response.setRequestId(request.getId());
        response.setPaymentTransactionId(transactionId);
        response.setPaymentMethod(request.getPaymentMethod());
        response.setPaid(true);
        response.setAmount(request.getEstimatedPrice());

        return response;
    }

    // =========================
// GET PAYMENT STATUS
// =========================
    public boolean isPaid(Integer requestId) {

        CarServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        return request.getInitialPaid(); // 🔥 الأفضل
    }
}