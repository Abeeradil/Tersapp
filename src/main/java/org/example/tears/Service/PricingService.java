package org.example.tears.Service;

import org.example.tears.Enums.ServiceOption;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

    public int calculatePreview(String serviceOption, boolean hydraulicTruck) {

        ServiceOption option = ServiceOption.valueOf(serviceOption);

        int total = option.getPrice();

        if (hydraulicTruck) {
            total += 100;
        }

        return total;
    }
}