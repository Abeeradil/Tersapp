package org.example.tears.Service;

import lombok.AllArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.WarrantyDetailsDto;
import org.example.tears.DTO.WarrantyListDto;
import org.example.tears.DTO.WarrantyRequestDto;
import org.example.tears.DTO.WarrantyResponseDto;
import org.example.tears.Enums.WarrantyProblemType;
import org.example.tears.Enums.WarrantyStatus;
import org.example.tears.Model.*;
import org.example.tears.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class WarrantyService {

    private final CarServiceRequestRepository requestRepo;
    private final WarrantyRepository warrantyRepo;
    private final FileStorageService fileStorageService;
    private final WarrantyImageRepository warrantyImageRepo;
    private final WarrantyStatusHistoryRepository historyRepo;
    private final NotificationService notificationService;


    @Transactional
    public void createWarrantyRequest(
            Integer requestId,
            WarrantyRequestDto dto,
            List<MultipartFile> images,
            Customer customer
    ) {

        CarServiceRequest request =
                requestRepo.findById(requestId)
                        .orElseThrow(() ->
                                new ApiException("الطلب غير موجود"));

        if (!request.getCustomer().getId().equals(customer.getId())) {
            throw new ApiException("غير مصرح لك");
        }

        WarrantyRequest warranty = new WarrantyRequest();

        warranty.setRequest(request);
        warranty.setCustomer(customer);

        warranty.setProblemType(dto.getProblemType());

        warranty.setDescription(dto.getDescription());

        warranty.setStatus(WarrantyStatus.PENDING_REVIEW);

        warranty.setCreatedAt(LocalDateTime.now());
        warranty.setUpdatedAt(LocalDateTime.now());

        warrantyRepo.save(warranty);

        saveImages(warranty, images);

        saveHistory(
                warranty,
                WarrantyStatus.PENDING_REVIEW
        );
    }

    private void saveImages(
            WarrantyRequest warranty,
            List<MultipartFile> images
    ) {

        if (images == null || images.isEmpty()) {
            return;
        }

        for (MultipartFile image : images) {

            String url = fileStorageService.saveFile(image,"receipts");

            WarrantyImage warrantyImage =
                    new WarrantyImage();

            warrantyImage.setWarrantyRequest(warranty);
            warrantyImage.setImageUrl(url);

            warrantyImageRepo.save(warrantyImage);
        }
    }

    private void saveHistory(
            WarrantyRequest warranty,
            WarrantyStatus status
    ) {

        WarrantyStatusHistory history =
                new WarrantyStatusHistory();

        history.setWarrantyRequest(warranty);

        history.setStatus(status);

        history.setChangedAt(LocalDateTime.now());

        historyRepo.save(history);
    }

    @Transactional(readOnly = true)
    public List<WarrantyListDto> getMyWarrantyRequests(
            Customer customer
    ) {

        List<WarrantyRequest> warranties =
                warrantyRepo.findByCustomer_IdOrderByCreatedAtDesc(
                        customer.getId()
                );

        List<WarrantyListDto> list = new ArrayList<>();

        for (WarrantyRequest warranty : warranties) {

            WarrantyListDto dto =
                    new WarrantyListDto();

            dto.setId(
                    warranty.getId()
            );

            dto.setOrderNumber(
                    warranty.getRequest().getOrderNumber()
            );

            dto.setProblemType(
                    warranty.getProblemType()
            );

            dto.setStatus(
                    warranty.getStatus()
            );

            dto.setCreatedAt(
                    warranty.getCreatedAt()
            );

            list.add(dto);
        }

        return list;
    }

    @Transactional(readOnly = true)
    public List<WarrantyResponseDto> getCustomerWarrantyRequests(
            Customer customer
    ) {

        List<WarrantyRequest> requests =
                warrantyRepo.findByCustomer_IdOrderByCreatedAtDesc(customer.getId());

        List<WarrantyResponseDto> list = new ArrayList<>();

        for (WarrantyRequest w : requests) {

            WarrantyResponseDto dto = new WarrantyResponseDto();

            dto.setId(w.getId());
            dto.setOrderNumber(w.getRequest().getOrderNumber());
            dto.setProblemType(w.getProblemType());
            dto.setStatus(w.getStatus());
            dto.setCreatedAt(w.getCreatedAt());

            list.add(dto);
        }

        return list;
    }
    @Transactional(readOnly = true)
    public WarrantyDetailsDto details(
            Integer warrantyId,
            Customer customer
    ) {

        WarrantyRequest warranty =
                warrantyRepo.findById(warrantyId)
                        .orElseThrow(() ->
                                new ApiException("طلب الضمان غير موجود"));

        if (!warranty.getCustomer().getId().equals(customer.getId())) {
            throw new ApiException("غير مصرح لك");
        }

        WarrantyDetailsDto dto = new WarrantyDetailsDto();

        dto.setId(warranty.getId());
        dto.setOrderNumber(warranty.getRequest().getOrderNumber());
        dto.setProblemType(warranty.getProblemType());
        dto.setDescription(warranty.getDescription());
        dto.setStatus(warranty.getStatus());
        dto.setRejectReason(warranty.getRejectReason());
        dto.setCreatedAt(warranty.getCreatedAt());

        return dto;
    }

    @Transactional
    public void approveWarranty(
            Integer warrantyId,
            Employee employee
    ){

        WarrantyRequest warranty =
                warrantyRepo.findById(warrantyId)
                        .orElseThrow(() ->
                                new ApiException("طلب الضمان غير موجود"));

        if(warranty.getStatus()!=WarrantyStatus.PENDING_REVIEW){
            throw new ApiException("تمت معالجة الطلب مسبقاً");
        }

        warranty.setStatus(WarrantyStatus.APPROVED);

        warranty.setApprovedBy(employee);
        warranty.setApprovedAt(LocalDateTime.now());

        warrantyRepo.save(warranty);

        notificationService.send(
                warranty.getRequest()
                        .getCustomer()
                        .getUser(),
                "تمت الموافقة على طلب الضمان"
        );
    }

    @Transactional
    public void rejectWarranty(
            Integer warrantyId,
            Employee employee,
            String reason
    ){

        WarrantyRequest warranty =
                warrantyRepo.findById(warrantyId)
                        .orElseThrow(() ->
                                new ApiException("طلب الضمان غير موجود"));

        if(warranty.getStatus()!=WarrantyStatus.PENDING_REVIEW){
            throw new ApiException("تمت معالجة الطلب مسبقاً");
        }

        warranty.setStatus(WarrantyStatus.REJECTED);

        warranty.setApprovedBy(employee);

        warranty.setApprovedAt(LocalDateTime.now());

        warranty.setRejectReason(reason);

        warrantyRepo.save(warranty);

        notificationService.send(
                warranty.getRequest()
                        .getCustomer()
                        .getUser(),
                "تم رفض طلب الضمان"
        );
    }




}
