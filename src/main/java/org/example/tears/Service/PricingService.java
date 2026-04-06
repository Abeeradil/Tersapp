package org.example.tears.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.Enums.PricingStatus;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Employee;
import org.example.tears.Model.RequestPart;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.RequestPartRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PricingService {


    private final RequestPartRepository partRepo;
    private final CarServiceRequestRepository requestRepo;


    @Transactional
    public void startPricing(Integer requestId, Employee pricingEmp) {

        CarServiceRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        // تعيين الموظف المسؤول عن التسعير
        req.setAssignedPricingEmployee(pricingEmp);
        // تغيير الحالة
        req.setPricingStatus(PricingStatus.PRICING);

        requestRepo.save(req);
    }



    @Transactional
    public void setFinalPrice(
            Integer partId,
            Integer price
    ) {

        RequestPart part = partRepo.findById(partId)
                .orElseThrow();

        part.setFinalPrice(price);

        partRepo.save(part);
    }


    @Transactional
    public void finishPricing(Integer requestId) {

        CarServiceRequest req = requestRepo.findById(requestId)
                .orElseThrow();

        req.setPricingStatus(PricingStatus.PRICED);

        requestRepo.save(req);
    }
}