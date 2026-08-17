package org.example.tears.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.AddPartsDto;
import org.example.tears.DTO.PartDto;
import org.example.tears.DTO.PartReportDto;
import org.example.tears.DTO.PartsDetailsDto;
import org.example.tears.Enums.*;
import org.example.tears.Mapper.RequestMapper;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Employee;
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
    private final NotificationService notificationService;
    private final RequestWorkflowService workflowService;
    private final RequestMapper requestMapper;


    // إضافة قطعة
        @Transactional
        public void addParts(Integer requestId, AddPartsDto dto) {

            CarServiceRequest req = requestRepo.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));

            if(req.getStaffStatus() != StaffRequestStatus.PARTS_REGISTERING &&
                    req.getStaffStatus() != StaffRequestStatus.REPAIRING){

                throw new RuntimeException("لا يمكن تسجيل القطع في هذه المرحلة");
            }
            /**
            if(dto.getProblemDescription() == null ||
                    dto.getProblemDescription().isBlank()){

                throw new RuntimeException("وصف المشكلة مطلوب");
            }
            **/

            if(dto.getParts() == null || dto.getParts().isEmpty()){

                throw new RuntimeException("يجب إضافة قطعة واحدة على الأقل");
            }

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
                part.setLaborCost(p.getLaborCost());



                partRepo.save(part);
            }
            Employee pricingEmployee =
                    workflowService.getLeastBusyPricingEmployee();

            req.setAssignedPricingEmployee(pricingEmployee);
            req.setCurrentEmployee(pricingEmployee);

            req.setStaffStatus(StaffRequestStatus.PRICING);
            req.setPricingStatus(PricingStatus.NEW);

            requestRepo.save(req);

            notificationService.send(
                    pricingEmployee.getUser(),
                    NotificationType.REQUEST_ASSIGNED,
                    NotificationCategory.REQUEST,
                    "تم إسناد طلب جديد للتسعير",
                    "تم إسناد الطلب رقم #" + req.getId() + " إليك للتسعير.",
                    NotificationActionType.OPEN_ENTITY,
                    NotificationEntityType.REQUEST,
                    req.getId().toString(),
                    NotificationSection.REQUESTS
            );
        }



        // عرض القطع
        public PartsDetailsDto getParts(
                Integer requestId,
                Employee employee
        ){
            CarServiceRequest req = requestRepo.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("الطلب غير موجود"));
            if (employee.getEmployeeRole() == EmployeeRole.PRICING) {

                if (req.getAssignedPricingEmployee() == null ||
                        !req.getAssignedPricingEmployee().getId().equals(employee.getId())) {

                    throw new ApiException("غير مصرح لك");
                }
            }
            List<RequestPart> parts = partRepo.findByRequestId(requestId);

            PartsDetailsDto dto = new PartsDetailsDto();
            dto.setNotes(
                    requestMapper.getRequestNotes(req)
            );
            List<PartReportDto> list = new ArrayList<>();

            int totalQuantity = 0;
            int totalLabor = 0;
            int totalPartsPrice = 0;

            for(RequestPart part : parts){

                PartReportDto p = new PartReportDto();

                p.setPartId(part.getId());
                p.setName(part.getName());
                p.setType(part.getType());
                p.setQuantity(part.getQuantity());

                p.setFinalPrice(part.getFinalPrice());

                p.setLaborCost(part.getLaborCost());
                p.setPriced(part.getPriced());

                Integer totalPrice = null;

                if(part.getFinalPrice() != null){
                    totalPrice = part.getFinalPrice() * part.getQuantity();
                    totalPartsPrice += totalPrice;
                }

                p.setTotalPrice(totalPrice);

                totalQuantity += part.getQuantity();
                totalLabor += part.getLaborCost();

                list.add(p);
            }

            boolean priced = parts.stream()
                    .allMatch(RequestPart::getPriced);

            dto.setPriced(priced);
            dto.setPricingStatus(req.getPricingStatus());
            dto.setParts(list);
            dto.setTotalParts(totalQuantity);
            dto.setTotalLabor(totalLabor);
            dto.setTotalPartsPrice(totalPartsPrice);
            dto.setGrandTotal(totalPartsPrice + totalLabor);

            return dto;
        }

}
