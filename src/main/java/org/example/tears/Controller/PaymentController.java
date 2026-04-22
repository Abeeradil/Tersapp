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
@RequestMapping("api/v1/tears/payment")
@RequiredArgsConstructor

public class PaymentController {
    final private PaymentService paymentService;

    // =========================
    // INITIATE
    // =========================
    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponseDto> initiate(@RequestBody CreatePaymentSessionDto dto) {
        return ResponseEntity.ok(paymentService.initiatePayment(dto));
    }

    // =========================
    // CONFIRM
    // =========================
//    @PostMapping("/confirm")
//    public ResponseEntity<PaymentResponseDto> confirm(@RequestParam String transactionId) {
//        return ResponseEntity.ok(paymentService.confirm(transactionId));
//    }
//
//    // =========================
//    // STATUS
//    // =========================
//    @GetMapping("/status/{requestId}")
//    public ResponseEntity<Boolean> status(@PathVariable Integer requestId) {
//        return ResponseEntity.ok(paymentService.isPaid(requestId));
//    }
    // =========================
}