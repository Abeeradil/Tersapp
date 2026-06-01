package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.CreatePaymentRequestDto;
import org.example.tears.DTO.CreatePaymentSessionDto;
import org.example.tears.DTO.PaymentResponseDto;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Service.MoyasarPaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/v1/tears/payment")
@RequiredArgsConstructor

public class PaymentController {

    private final MoyasarPaymentService paymentService;

    // إنشاء جلسة دفع
    @PostMapping("/create/{requestId}")
    public String create(@PathVariable Integer requestId) {
        return paymentService.createPayment(requestId);
    }

    // webhook من Moyasar
    @PostMapping("/webhook")
    public void webhook(@RequestBody Map<String, Object> payload) {

        String status = (String) payload.get("status");
        String id = (String) payload.get("id");

        if ("paid".equals(status)) {
            paymentService.confirmPayment(id);
        }
    }
    
}