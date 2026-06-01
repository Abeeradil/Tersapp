package org.example.tears.Controller;

import lombok.RequiredArgsConstructor;
import org.example.tears.InpDTO.CreateRequestStepDto;
import org.example.tears.Service.PaymentIntentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentIntentController {

    private final PaymentIntentService service;

    @PostMapping("/create")
    public Map<String, String> create(@RequestBody CreateRequestStepDto dto) {
        return service.createPaymentIntent(dto);
    }
}