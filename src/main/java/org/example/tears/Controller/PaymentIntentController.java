package org.example.tears.Controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.*;
import org.example.tears.InpDTO.CreateRequestStepDto;
import org.example.tears.Model.User;
import org.example.tears.OutDTO.RequestResponseDto;
import org.example.tears.Service.AuthService;
import org.example.tears.Service.PaymentIntentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @RequestHeader Map<String, String> headers,
            @RequestBody String body
    ) {

        System.out.println("========== WEBHOOK ==========");
        System.out.println(headers);
        System.out.println(body);

        return ResponseEntity.ok("OK");
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