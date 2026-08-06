package org.example.tears.Service;

import lombok.AllArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.WarrantyDetailsDto;
import org.example.tears.DTO.WarrantyRequestDto;
import org.example.tears.DTO.WarrantyResponseDto;
import org.example.tears.Enums.*;
import org.example.tears.Mapper.RequestMapper;
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
    private final TicketRepository ticketRepository;
    private final EmployeeRepository employeeRepo;
    private final UserRepository userRepo;
    private final RequestMapper requestMapper;

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

        if (!requestMapper.isWarrantyEligible(request)) {
            throw new ApiException("انتهت فترة الضمان");
        }

        if (request.getCustomerStatus() != CustomerRequestStatus.DELIVERED) {
            throw new ApiException("لا يمكن إنشاء طلب ضمان");
        }

        if (warrantyRepo.existsByRequestId(requestId)) {
            throw new ApiException("تم إنشاء طلب ضمان مسبقاً");
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

        // ============================
        // إنشاء التذكرة تلقائياً
        // ============================

        Ticket ticket = new Ticket();

        ticket.setWarrantyRequest(warranty);

        ticket.setRequest(request);

        ticket.setCustomer(customer);

        ticket.setProblemType(TicketProblemType.OTHER);

        ticket.setPriority(TicketPriority.IMPORTANT);

        ticket.setStatus(TicketStatus.ACTIVE);

        ticket.setDescription(dto.getDescription());

        ticket.setCreatedAt(LocalDateTime.now());

        ticket.setUpdatedAt(LocalDateTime.now());

        ticket.setLocation(request.getLocation());

        ticket.setServiceOption(request.getServiceOption());

        ticket = ticketRepository.save(ticket);

        ticket.setTicketNumber(
                String.format("TK-%06d", ticket.getId())
        );

        ticketRepository.save(ticket);


        // ============================
        // إشعارات
        // ============================

        String notify =
                "يوجد طلب ضمان جديد #" + ticket.getTicketNumber();

        notifyEmployees(
                EmployeeRole.SUPPORT,
                notify
        );

        List<User> admins =
                userRepo.findByRole(UserRole.ADMIN);

        for (User admin : admins) {
            notificationService.send(
                    admin,
                    notify
            );
        }

        notificationService.send(
                customer.getUser(),
                "تم استلام طلب الضمان بنجاح، وسيتم التواصل معك عبر المحادثة."
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

        if (employee.getEmployeeRole() != EmployeeRole.SUPPORT
                && employee.getUser().getRole() != UserRole.ADMIN) {
            throw new ApiException("غير مصرح لك بمعالجة طلبات الضمان");
        }

        if(warranty.getStatus()!=WarrantyStatus.PENDING_REVIEW){
            throw new ApiException("تمت معالجة الطلب مسبقاً");
        }

        warranty.setStatus(WarrantyStatus.APPROVED);

        warranty.setApprovedBy(employee);
        warranty.setApprovedAt(LocalDateTime.now());
        warranty.setUpdatedAt(LocalDateTime.now());

        saveHistory(
                warranty,
                WarrantyStatus.APPROVED
        );

        Employee technician =
                warranty.getRequest().getAssignedEmployee();

        warranty.setAssignedEmployee(technician);

        warrantyRepo.save(warranty);

        if (warranty.getRequest().getAssignedEmployee() != null) {

            notificationService.send(
                    warranty.getRequest()
                            .getAssignedEmployee()
                            .getUser(),
                    "تمت الموافقة على طلب ضمان للطلب #"
                            + warranty.getRequest().getOrderNumber()
            );
        }

        Ticket ticket =
                ticketRepository.findByWarrantyRequest_Id(warrantyId)
                        .orElseThrow(() ->
                                new ApiException("التذكرة غير موجودة"));
        ticket.setStatus(TicketStatus.SOLVED);
        ticket.setSolvedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        ticketRepository.save(ticket);

        notificationService.send(
                warranty.getRequest()
                        .getCustomer()
                        .getUser(),
                "تمت الموافقة على طلب الضمان للطلب #"
                        + warranty.getRequest().getOrderNumber()
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

        if (employee.getEmployeeRole() != EmployeeRole.SUPPORT
                && employee.getUser().getRole() != UserRole.ADMIN) {
            throw new ApiException("غير مصرح لك بمعالجة طلبات الضمان");
        }

        if(warranty.getStatus()!=WarrantyStatus.PENDING_REVIEW){
            throw new ApiException("تمت معالجة الطلب مسبقاً");
        }

        warranty.setStatus(WarrantyStatus.REJECTED);

        warranty.setApprovedBy(employee);

        warranty.setApprovedAt(LocalDateTime.now());

        warranty.setRejectReason(reason);
        saveHistory(
                warranty,
                WarrantyStatus.REJECTED
        );
        warranty.setUpdatedAt(LocalDateTime.now());
        warrantyRepo.save(warranty);

        Ticket ticket =
                ticketRepository.findByWarrantyRequest_Id(warrantyId)
                        .orElseThrow(() ->
                                new ApiException("التذكرة غير موجودة"));

        ticket.setStatus(TicketStatus.SOLVED);
        ticket.setSolvedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        ticketRepository.save(ticket);

        notificationService.send(
                warranty.getRequest()
                        .getCustomer()
                        .getUser(),
                "تم رفض طلب الضمان للطلب #"
                        + warranty.getRequest().getOrderNumber()
                        + "\nالسبب: " + reason
        );

    }

    private void notifyEmployees(EmployeeRole role, String message) {

        List<Employee> employees =
                employeeRepo.findByEmployeeRole(role);

        for (Employee employee : employees) {
            notificationService.send(
                    employee.getUser(),
                    message
            );
        }
    }




}
