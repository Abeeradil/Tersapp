package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.CreatePaymentSessionDto;
import org.example.tears.DTO.PaymentResponseDto;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/tears/pay")
@RequiredArgsConstructor

public class PaymentController {
    final private PaymentService paymentService;

    @PostMapping("/pay")
    public ResponseEntity<PaymentResponseDto> pay(@RequestBody CreatePaymentSessionDto dto) {
        PaymentResponseDto response = paymentService.initiatePayment(dto);

        return ResponseEntity.ok(response);
    }


}
