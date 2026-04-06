package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Model.PaymentGatewaySetting;
import org.example.tears.Repository.PaymentGatewaySettingRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentSettingService {

    private final PaymentGatewaySettingRepository repo;

    public PaymentGatewaySetting saveSetting(PaymentGatewaySetting setting) {
        return repo.save(setting);
    }

    public PaymentGatewaySetting getSetting(String provider) {
        return repo.findByProvider(provider)
                .orElseThrow(() -> new RuntimeException("الإعدادات غير موجودة"));
    }
}