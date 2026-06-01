package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.InpDTO.CreateRequestStepDto;
import org.example.tears.Service.PaymentIntentService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tears/payment")
@RequiredArgsConstructor
public class PaymentIntentController {

        private final PaymentIntentService service;

        // 1. create intent
        @PostMapping("/intent")
        public Map<String, String> create(@RequestBody CreateRequestStepDto dto) {
            return service.createIntent(dto);
        }

        // 2. create checkout
        @PostMapping("/checkout/{id}")
        public Map<String, String> checkout(@PathVariable Integer id) {
            return service.createCheckout(id);
        }

        // 3. callback (Moyasar)
        @PostMapping("/callback")
        public void callback(@RequestParam String id, @RequestParam String status) {
            if ("paid".equalsIgnoreCase(status)) {
                service.confirmPayment(id);
            }
        }
    }