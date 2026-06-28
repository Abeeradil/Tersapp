package org.example.tears.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.AddPartsDto;
import org.example.tears.DTO.PartDto;
import org.example.tears.Enums.StaffRequestStatus;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.RequestPart;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.RequestPartRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartsService {
    private final RequestPartRepository partRepo;
        private final CarServiceRequestRepository requestRepo;


        // إضافة قطعة
        @Transactional
        public void addParts(Integer requestId, AddPartsDto dto) {

            CarServiceRequest req = requestRepo.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

            if(req.getStaffStatus() != StaffRequestStatus.PARTS_REGISTERING &&
                    req.getStaffStatus() != StaffRequestStatus.REPAIRING){

                throw new RuntimeException("لا يمكن تسجيل القطع في هذه المرحلة");
            }

            if(dto.getProblemDescription() == null ||
                    dto.getProblemDescription().isBlank()){

                throw new RuntimeException("وصف المشكلة مطلوب");
            }

            if(dto.getParts() == null || dto.getParts().isEmpty()){

                throw new RuntimeException("يجب إضافة قطعة واحدة على الأقل");
            }

            req.setProblemDescription(dto.getProblemDescription());

            for (PartDto p : dto.getParts()) {

                if(p.getName() == null || p.getName().isBlank()){
                    throw new RuntimeException("اسم القطعة مطلوب");
                }

                if(p.getQuantity() == null || p.getQuantity() <= 0){
                    throw new RuntimeException("الكمية غير صحيحة");
                }

                if(p.getEstimatedPrice() == null || p.getEstimatedPrice() < 0){
                    throw new RuntimeException("السعر غير صحيح");
                }

                RequestPart part = new RequestPart();

                part.setRequest(req);
                part.setName(p.getName());
                part.setType(p.getType());
                part.setQuantity(p.getQuantity());
                part.setEstimatedPrice(p.getEstimatedPrice());

                part.setLaborCost(
                        calculateLaborCost(p.getQuantity())
                );

                partRepo.save(part);
            }

            requestRepo.save(req);
        }



        // عرض القطع
        public List<RequestPart> getParts(Integer requestId) {

            return partRepo.findByRequestId(requestId);
        }

    private Integer calculateLaborCost(Integer quantity){

        if(quantity == null || quantity <= 0){
            return 0;
        }

        return quantity * 25;
    }
}
