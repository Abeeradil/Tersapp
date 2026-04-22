package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.example.tears.DTO.PartDto;
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
        public void addPart(Integer requestId, PartDto dto) {

            CarServiceRequest req = requestRepo.findById(requestId)
                    .orElseThrow();

            RequestPart part = new RequestPart();

            part.setRequest(req);
            part.setName(dto.getName());
            part.setType(dto.getType());
            part.setQuantity(dto.getQuantity());
            part.setEstimatedPrice(dto.getEstimatedPrice());
            part.setLaborCost(dto.getLaborCost());

            partRepo.save(part);
        }


        // عرض القطع
        public List<RequestPart> getParts(Integer requestId) {

            return partRepo.findByRequestId(requestId);
        }
    }
