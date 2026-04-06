package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.CreatePaymentSessionDto;
import org.example.tears.DTO.PaymentResponseDto;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Enums.PaymentMethod;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final CarServiceRequestRepository requestRepository;

    public PaymentResponseDto initiatePayment(CreatePaymentSessionDto dto) {
        CarServiceRequest request = requestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

        PaymentMethod method = PaymentMethod.valueOf(dto.getPaymentMethod().toString().toUpperCase());
        request.setPaymentMethod(method);

        PaymentResponseDto response = new PaymentResponseDto();
        response.setRequestId(request.getId());
        response.setPaymentMethod(method);

        try {
            switch (method) {
                case APPLE_PAY -> {
                    String txId = initiateApplePay(request, dto.isInitialPayment());
                    response.setPaymentTransactionId(txId);
                    markPaid(request, dto.isInitialPayment(), txId);
                }
                case MADA -> {
                    String txId = initiateMadaPayment(request, dto.isInitialPayment());
                    response.setPaymentTransactionId(txId);
                    markPaid(request, dto.isInitialPayment(), txId);
                }
                case VISA -> {
                    String txId = initiateVisaPayment(request, dto.isInitialPayment());
                    response.setPaymentTransactionId(txId);
                    markPaid(request, dto.isInitialPayment(), txId);
                }
                case TABBY -> {
                    String txId = initiateTabby(request, dto.isInitialPayment());
                    response.setPaymentTransactionId(txId);
                    markPaid(request, dto.isInitialPayment(), txId);
                }
                case TAMARA -> {
                    String txId = initiateTamara(request, dto.isInitialPayment());
                    response.setPaymentTransactionId(txId);
                    markPaid(request, dto.isInitialPayment(), txId);
                }
                case BASITAH -> {
                    String txId = initiateBasitah(request, dto.isInitialPayment());
                    response.setPaymentTransactionId(txId);
                    markPaid(request, dto.isInitialPayment(), txId);
                }
                case WALLET -> {
                    int amountToPay = dto.isInitialPayment() ? request.getEstimatedPrice() : request.getFinalPrice();
                    Integer userId = request.getCustomer().getUser().getId(); // هنا بدال getCustomerId()

                    if (!userHasEnoughBalance(userId, amountToPay)) {
                        response.setPaid(false);
                        response.setAmount(amountToPay);
                        response.setErrorMessage("رصيد المحفظة غير كافٍ");
                        return response;
                    }

                    deductFromWallet(userId, amountToPay);
                    markPaid(request, dto.isInitialPayment(), "WALLET-" + request.getId());
                    response.setPaid(true);
                    response.setAmount(amountToPay);
                }

            }

            requestRepository.save(request);
            return response;

        } catch (Exception e) {
            response.setErrorMessage(e.getMessage());
            return response;
        }
    }

    // ============================
    // علامة الدفع لكل دفعة
    // ============================
    private void markPaid(CarServiceRequest request, boolean initialPayment, String transactionId) {
        if (initialPayment) {
            request.setInitialPaid(true);
            request.setInitialTransactionId(transactionId);
        } else {
            request.setFinalPaid(true);
            request.setFinalTransactionId(transactionId);
        }
    }

    // ============================
    // توابع الدفع (Placeholder)
    // ============================
    private String initiateApplePay(CarServiceRequest request, boolean initial) { return "APPLE-" + request.getId(); }
    private String initiateMadaPayment(CarServiceRequest request, boolean initial) { return "MADA-" + request.getId(); }
    private String initiateVisaPayment(CarServiceRequest request, boolean initial) { return "VISA-" + request.getId(); }
    private String initiateTabby(CarServiceRequest request, boolean initial) { return "TABBY-" + request.getId(); }
    private String initiateTamara(CarServiceRequest request, boolean initial) { return "TAMARA-" + request.getId(); }
    private String initiateBasitah(CarServiceRequest request, boolean initial) { return "BASITAH-" + request.getId(); }

    // ============================
    // رصيد المحفظة (Placeholder)
    // ============================
    private boolean userHasEnoughBalance(Integer userId, int amount) { return true; }
    private void deductFromWallet(Integer userId, int amount) { /* placeholder */ }

}
