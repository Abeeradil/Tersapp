package org.example.tears.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.PricingPartDto;
import org.example.tears.DTO.PricingRequestDto;
import org.example.tears.Enums.PricingStatus;
import org.example.tears.Enums.StaffRequestStatus;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Employee;
import org.example.tears.Model.RequestPart;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.RequestPartRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestPricingService {

    private final CarServiceRequestRepository requestRepo;
    private final RequestPartRepository partRepo;

//    @Transactional
//    public void startPricing(Integer requestId, Employee employee) {
//
//        CarServiceRequest request = requestRepo.findById(requestId)
//                .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));
//
//        // التأكد أن الطلب مسند لهذا الموظف
//        if (request.getAssignedPricingEmployee() == null ||
//                !request.getAssignedPricingEmployee().getId().equals(employee.getId())) {
//
//            throw new RuntimeException("الطلب غير مسند لك");
//        }
//
//        // لا يبدأ إلا إذا كان جديد
//        if (request.getPricingStatus() != PricingStatus.NEW) {
//
//            throw new RuntimeException("تم بدء التسعير مسبقاً");
//        }
//
//        request.setPricingStatus(PricingStatus.PRICING);
//
//        // هذه الحالة أصلاً تكون PRICING بعد إسناد الطلب،
//        // لكن نعيد ضبطها احتياطاً
//        request.setStaffStatus(StaffRequestStatus.PRICING);
//
//        request.setCurrentEmployee(employee);
//
//        request.setLastUpdated(LocalDateTime.now());
//
//        requestRepo.save(request);
//    }

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

    @Transactional
    public void finishPricing(Integer requestId) {

        CarServiceRequest request = requestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

        List<RequestPart> parts = partRepo.findByRequestId(requestId);

        if (parts.isEmpty()) {
            throw new RuntimeException("لا توجد قطع لهذا الطلب");
        }

        if (parts.stream().anyMatch(part -> part.getFinalPrice() == null)) {
            throw new RuntimeException("يجب تسعير جميع القطع قبل إنهاء التسعير");
        }

        request.setPricingStatus(PricingStatus.PRICED);

        requestRepo.save(request);
    }
}