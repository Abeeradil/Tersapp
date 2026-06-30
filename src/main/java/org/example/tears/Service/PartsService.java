package org.example.tears.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.AddPartsDto;
import org.example.tears.DTO.PartDto;
import org.example.tears.DTO.PartReportDto;
import org.example.tears.DTO.PartsDetailsDto;
import org.example.tears.Enums.StaffRequestStatus;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.RequestPart;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.RequestPartRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
            req.setStaffStatus(StaffRequestStatus.PARTS_REGISTERING);
            req.setLastUpdated(LocalDateTime.now());

            requestRepo.save(req);
            for (PartDto p : dto.getParts()) {

                if(p.getName() == null || p.getName().isBlank()){
                    throw new RuntimeException("اسم القطعة مطلوب");
                }

                if(p.getQuantity() == null || p.getQuantity() <= 0){
                    throw new RuntimeException("الكمية غير صحيحة");
                }

                RequestPart part = new RequestPart();

                part.setRequest(req);
                part.setName(p.getName());
                part.setType(p.getType());
                part.setQuantity(p.getQuantity());

                part.setLaborCost(
                        calculateLaborCost(p.getQuantity())
                );

                partRepo.save(part);
            }


            requestRepo.save(req);
        }



        // عرض القطع
        public PartsDetailsDto getParts(Integer requestId){

            CarServiceRequest req = requestRepo.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

            List<RequestPart> parts = partRepo.findByRequestId(requestId);

            PartsDetailsDto dto = new PartsDetailsDto();

            dto.setProblemDescription(req.getProblemDescription());

            List<PartReportDto> list = new ArrayList<>();

            int totalQuantity = 0;
            int totalLabor = 0;

            for(RequestPart part : parts){

                PartReportDto p = new PartReportDto();

                p.setName(part.getName());
                p.setType(part.getType());
                p.setQuantity(part.getQuantity());
                p.setLaborCost(part.getLaborCost());

                totalQuantity += part.getQuantity();
                totalLabor += part.getLaborCost();
                list.add(p);
            }

            dto.setParts(list);
            dto.setTotalParts(totalQuantity);
            dto.setTotalLabor(totalLabor);

            return dto;
        }

        private Integer calculateLaborCost(Integer quantity){
            return quantity * 25;
        }

}
