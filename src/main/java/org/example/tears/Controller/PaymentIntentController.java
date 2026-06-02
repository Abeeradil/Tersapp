package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.CheckoutResponse;
import org.example.tears.DTO.CreatePaymentIntentRequest;
import org.example.tears.InpDTO.CreateRequestStepDto;
import org.example.tears.Service.PaymentIntentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tears/payment")
@RequiredArgsConstructor
public class PaymentIntentController {

        private final PaymentIntentService paymentIntentService;

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> createCheckout(
            @RequestBody CreatePaymentIntentRequest request
    ) {
        return ResponseEntity.ok(paymentIntentService.createCheckout(request));
    }

    @PostMapping("/moyasar/callback")
    public ResponseEntity<String> moyasarCallback(@RequestBody Map<String, Object> payload) {
        paymentIntentService.handleInvoiceCallback(payload);
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