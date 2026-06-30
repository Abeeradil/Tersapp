package org.example.tears.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.PricingPartDto;
import org.example.tears.DTO.PricingRequestDto;
import org.example.tears.Enums.PricingStatus;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Employee;
import org.example.tears.Model.RequestPart;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.RequestPartRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestPricingService {

    private final CarServiceRequestRepository requestRepo;
    private final RequestPartRepository partRepo;

    @Transactional
    public void pricingParts(
            Integer requestId,
            PricingRequestDto dto
    ){

        CarServiceRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

        if(request.getPricingStatus() != PricingStatus.PRICING){
            throw new RuntimeException("الطلب ليس في مرحلة التسعير");
        }

        int totalParts = 0;
        int totalLabor = 0;

        for(PricingPartDto item : dto.getParts()){

            RequestPart part = partRepo.findById(item.getPartId())
                    .orElseThrow(() -> new RuntimeException("القطعة غير موجودة"));

            part.setFinalPrice(item.getFinalPrice());
            part.setPriced(true);

            partRepo.save(part);

            totalParts +=
                    part.getFinalPrice() * part.getQuantity();

            totalLabor += part.getLaborCost();
        }

        request.setFinalPrice(totalParts + totalLabor);

        requestRepo.save(request);
    }

        public void finishPricing(Integer requestId) {

            CarServiceRequest request = requestRepo.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Request not found"));

            request.setPricingStatus(PricingStatus.PRICED);

            requestRepo.save(request);
        }
    }