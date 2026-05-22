package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.Enums.PricingStatus;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Employee;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestPricingService {

    private final CarServiceRequestRepository requestRepository;

    public void startPricing(Integer requestId, Employee employee) {

        CarServiceRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        request.setPricingStatus(PricingStatus.PRICING);
        request.setAssignedPricingEmployee(employee);

        requestRepository.save(request);
    }

        public void finishPricing(Integer requestId) {

            CarServiceRequest request = requestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Request not found"));

            request.setPricingStatus(PricingStatus.PRICED);

            requestRepository.save(request);
        }
    }