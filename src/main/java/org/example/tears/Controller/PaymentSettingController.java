package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.PaymentGatewaySetting;
import org.example.tears.Service.PaymentSettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/payment-settings")
@RequiredArgsConstructor
public class PaymentSettingController {

    private final PaymentSettingService service;

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody PaymentGatewaySetting setting) {
        return ResponseEntity.ok(service.saveSetting(setting));
    }

    @GetMapping("/{provider}")
    public ResponseEntity<?> get(@PathVariable String provider) {
        return ResponseEntity.ok(service.getSetting(provider));
    }

    private String initiateApplePay(CarServiceRequest request) {
        PaymentGatewaySetting setting = service.getSetting("APPLE_PAY");
        String apiKey = setting.getApiKey();
        String secret  = setting.getSecretKey();

        // هنا تستدعين API مزود الدفع الحقيقي
        return "APPLE-SESSION-" + request.getId();
    }
}