package org.example.tears.Controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.*;
import org.example.tears.InpDTO.CreateRequestStepDto;
import org.example.tears.OutDTO.RequestResponseDto;
import org.example.tears.Service.AuthService;
import org.example.tears.Service.PaymentIntentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tears/payment")
@RequiredArgsConstructor
public class PaymentIntentController {

        private final PaymentIntentService paymentIntentService;
        private final AuthService authService;

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> createCheckout(
            HttpServletRequest request,
            @RequestBody CreateRequestStepDto dto
    ) {
        return ResponseEntity.ok(
                paymentIntentService.createCheckout(request, dto)
        );
    }

    @PostMapping("/moyasar/callback")
    public ResponseEntity<RequestResponseDto> moyasarCallback(
            @RequestBody Map<String, Object> payload
    ) {
        return ResponseEntity.ok(
                paymentIntentService.handleInvoiceCallback(payload)
        );
    }
    @GetMapping("/intent/{paymentIntentId}/result")
    public ResponseEntity<RequestResponseDto> getPaymentResult(
            @PathVariable Integer paymentIntentId
    ) {
        return ResponseEntity.ok(
                paymentIntentService.getPaymentResult(paymentIntentId)
        );
    }

    @PostMapping("/wallet/pay-request")
    public ResponseEntity<RequestResponseDto> payRequestWithWallet(
            HttpServletRequest request,
            @RequestBody CreateRequestStepDto dto
    ) {
        return ResponseEntity.ok(
                paymentIntentService.payRequestWithWallet(request, dto)
        );
    }

    @PostMapping("/requests/{requestId}/final-payment")
    public CheckoutResponse createFinalPayment(
            @PathVariable Integer requestId,
            HttpServletRequest request
    ) {

        return paymentIntentService.createFinalCheckout(
                requestId,
                request
        );
    }

    @PostMapping("/final/callback")
    public RequestResponseDto finalPaymentCallback(
            @RequestBody Map<String,Object> payload
    ){

        return paymentIntentService.handleFinalInvoiceCallback(payload);

    }


    @PostMapping("/mobile/prepare")
    public ResponseEntity<MobilePaymentResponse> prepareMobilePayment(
            HttpServletRequest request,
            @RequestBody CreateRequestStepDto dto
    ) {

        return ResponseEntity.ok(
                paymentIntentService.prepareMobilePayment(request, dto)
        );
    }

    @PostMapping("/mobile/confirm")
    public ResponseEntity<ConfirmMobilePaymentResponse> confirmMobilePayment(
            @RequestBody ConfirmMobilePaymentRequest request
    ) {

        return ResponseEntity.ok(
                paymentIntentService.confirmMobilePayment(request)
        );

    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestHeader("X-Moyasar-Token") String token,
            @RequestBody String body
    ) {
        paymentIntentService.handleWebhook(token, body);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/mobile/final/confirm")
    public ResponseEntity<ConfirmMobilePaymentResponse> confirmFinalMobilePayment(
            @RequestBody ConfirmMobilePaymentRequest dto
    ) {

        ConfirmMobilePaymentResponse response =
                paymentIntentService.confirmFinalMobilePayment(dto);


        return ResponseEntity.ok(response);
    }


    @PostMapping("/mobile/final/prepare/{requestId}")
    public ResponseEntity<FinalMobilePaymentResponse> prepareFinalMobilePayment(
            @PathVariable Integer requestId,
            HttpServletRequest request
    ) {

        FinalMobilePaymentResponse response =
                paymentIntentService.prepareFinalMobilePayment(
                        requestId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/success")
    public ResponseEntity<String> success() {
        return ResponseEntity.ok("Payment success. You can close this page.");
    }

    @GetMapping("/cancel")
    public ResponseEntity<String> cancel() {
        return ResponseEntity.ok("Payment cancelled.");
    }
}