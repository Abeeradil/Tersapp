package org.example.tears.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity

public class PaymentGatewaySetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String provider;        // APPLE_PAY, MADA, VISA, TABBY, TAMARA, WALLET
    private String apiKey;          // المفتاح اللي يجي من شركة الدفع
    private String secretKey;       // المفتاح السري
    private boolean enabled;        // مفعلة ولا لا

}
