package org.example.tears.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.*;
import org.example.tears.Enums.*;
import org.example.tears.Model.*;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.ChatMessageRepository;
import org.example.tears.Repository.TicketRepository;
import org.example.tears.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;

    private final CarServiceRequestRepository requestRepository;
    private final NotificationService notificationService;
    private final ChatMessageRepository chatMessageRepository;

    private final UserRepository userRepo;


    private final AuthService authService;
    private final ChatService chatService;

    @Transactional
    public TicketResponseDto createTicket(
            HttpServletRequest httpRequest,
            CreateTicketDto dto
    ) {

        User user = authService.getAuthenticatedUser(httpRequest);

        if (user.getEmployee() == null) {
            throw new ApiException("غير مصرح لك");
        }

        CarServiceRequest request =
                requestRepository.findById(dto.getRequestId())
                        .orElseThrow(() ->
                                new ApiException("الطلب غير موجود"));

        Optional<Ticket> activeTicket =
                ticketRepository.findByRequest_IdAndStatusIn(
                        request.getId(),
                        List.of(
                                TicketStatus.ACTIVE,
                                TicketStatus.IN_PROGRESS
                        )
                );

        if (activeTicket.isPresent()) {
            throw new ApiException("يوجد تذكرة مفتوحة لهذا الطلب");
        }

        Ticket ticket = new Ticket();

        ticket.setProblemType(dto.getProblemType());
        ticket.setPriority(dto.getPriority());
        ticket.setDescription(dto.getDescription());

        ticket.setStatus(TicketStatus.ACTIVE);

        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        ticket.setRequest(request);
        ticket.setCustomer(request.getCustomer());

        ticket.setCreatedByEmployee(user.getEmployee());
        ticket.setLocation(request.getLocation());
        ticket.setServiceOption(request.getServiceOption());

        // نحفظ أول مرة للحصول على الـ ID
        ticket = ticketRepository.save(ticket);

        // إنشاء رقم التذكرة
        ticket.setTicketNumber(
                String.format("TK-%06d", ticket.getId())
        );

        // حفظ رقم التذكرة
        ticket = ticketRepository.save(ticket);

        return mapToResponse(ticket);
    }

    public TicketResponseDto mapToResponse(Ticket ticket) {

        TicketResponseDto dto = new TicketResponseDto();

        dto.setId(ticket.getId());

        dto.setTicketNumber(ticket.getTicketNumber());

        dto.setOrderNumber(ticket.getRequest().getOrderNumber());

        dto.setRequestId(ticket.getRequest().getId());

        dto.setProblemType(ticket.getProblemType());

        dto.setPriority(ticket.getPriority());

        dto.setStatus(ticket.getStatus());

        dto.setCreatedAt(ticket.getCreatedAt());

        dto.setCarModel( ticket.getRequest() .getCar() .getModel() .getNameAr() );

        dto.setPlateArabic(
                formatCarArTitle(ticket.getRequest().getCar())
        );

        dto.setPlateEnglish(
                formatCarEnTitle(ticket.getRequest().getCar())
        );

        return dto;
    }
    public List<TicketResponseDto> getMyTickets(
            HttpServletRequest httpRequest
    ) {

        User user = authService.getAuthenticatedUser(httpRequest);

        if (user.getEmployee() == null) {
            throw new ApiException("غير مصرح لك");
        }

        return ticketRepository
                .findByCreatedByEmployee_IdOrderByCreatedAtDesc(
                        user.getEmployee().getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TicketDetailsDto getTicketDetails(Integer ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ApiException("التذكرة غير موجودة"));

        return mapToDetails(ticket);
    }

    private TicketDetailsDto mapToDetails(Ticket ticket) {

        TicketDetailsDto dto = new TicketDetailsDto();

        dto.setId(ticket.getId());

        dto.setTicketNumber(ticket.getTicketNumber());

        if (ticket.getServiceOption() != null){
            dto.setServiceOption(ticket.getServiceOption().name());
        }

        dto.setRequestId(ticket.getRequest().getId());

        dto.setOrderNumber(ticket.getRequest().getOrderNumber());

        dto.setCustomerName(
                ticket.getCustomer()
                        .getUser()
                        .getFullName()
        );

        dto.setCustomerPhone(
                ticket.getCustomer()
                        .getUser()
                        .getPhoneNumber()
        );


        dto.setProblemType(ticket.getProblemType());

        dto.setPriority(ticket.getPriority());

        dto.setStatus(ticket.getStatus());

        dto.setAcceptedByCustomerService(
                ticket.getAcceptedByCustomerService()
        );

        dto.setCarModel( ticket.getRequest() .getCar() .getModel() .getNameAr() );

        dto.setPlateArabic(
                formatCarArTitle(ticket.getRequest().getCar())
        );

        dto.setPlateEnglish(
                formatCarEnTitle(ticket.getRequest().getCar())
        );

        dto.setAcceptedAt(
                ticket.getAcceptedAt()
        );

        dto.setSolvedAt(
                ticket.getSolvedAt()
        );

        if (ticket.getLocation() != null) {

            dto.setAddress(ticket.getLocation().getAddress());

            dto.setCity(
                    extractCity(ticket.getLocation().getAddress())
            );
        }

        if (ticket.getAssignedSupportEmployee() != null) {

            dto.setAssignedSupportEmployeeName(
                    ticket.getAssignedSupportEmployee()
                            .getUser()
                            .getFullName()
            );

            dto.setAssignedSupportEmployeePhone(
                    ticket.getAssignedSupportEmployee()
                            .getUser()
                            .getPhoneNumber()
            );
        }

        dto.setDescription(ticket.getDescription());

        dto.setCreatedAt(ticket.getCreatedAt());

        dto.setUpdatedAt(ticket.getUpdatedAt());

        dto.setSolvedAt(ticket.getSolvedAt());

        if (ticket.getAssignedSupportEmployee() != null) {

            dto.setAssignedSupportEmployee(
                    ticket.getAssignedSupportEmployee()
                            .getUser()
                            .getFullName()
            );
        }

        return dto;
    }

    private String extractCity(String address) {

        if (address == null || address.isBlank()) {
            return address;
        }

        String[] parts = address.split("،");

        return parts[parts.length - 1].trim();
    }

    private String formatCarArTitle(Car car) {

        if (car == null) {
            return null;
        }

        String model = car.getModel().getNameAr();

        String plate = car.getPlateNumberArabic();

        if (plate == null || plate.isBlank()) {
            return model;
        }

        String[] parts = plate.trim().split("\\s+");

        String letters;

        if (parts.length >= 3) {
            letters = parts[0] + " " + parts[1] + " " + parts[2];
        } else {
            letters = plate;
        }

        return model + " - " + letters;
    }


    private String formatCarEnTitle(Car car) {

        if (car == null) {
            return null;
        }

        String model = car.getModel().getName();

        String plate = car.getPlateNumberEnglish();

        if (plate == null || plate.isBlank()) {
            return model;
        }

        String letters;

        if (plate.length() >= 3) {
            letters = plate.substring(0, 3);
        } else {
            letters = plate;
        }

        return model + " - " + letters;
    }


    @Transactional
    public void updateStatus(
            Integer ticketId,
            UpdateTicketStatusDto dto,
            HttpServletRequest httpRequest
    ) {

        User user = authService.getAuthenticatedUser(httpRequest);

        if (user.getEmployee() == null) {
            throw new ApiException("غير مصرح");
        }

        Employee employee = user.getEmployee();

        if (employee.getEmployeeRole() != EmployeeRole.SUPPORT) {
            throw new ApiException("هذه العملية خاصة بخدمة العملاء");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ApiException("التذكرة غير موجودة"));

        CarServiceRequest serviceRequest = ticket.getRequest();

        TicketStatus newStatus = dto.getStatus();

        // ============================
        // ACTIVE -> IN_PROGRESS
        // ============================

        if (ticket.getStatus() == TicketStatus.ACTIVE &&
                newStatus == TicketStatus.IN_PROGRESS) {

            if (ticket.getAssignedSupportEmployee() != null) {
                throw new ApiException("تم استلام التذكرة بواسطة موظف آخر");
            }

            ChatRoom room = chatService.createRoomIfNotExists(ticket);


            User admin = userRepo.findById(8)
                    .orElseThrow(() -> new ApiException("Admin غير موجود"));

            ChatMessage systemMessage = new ChatMessage();
            systemMessage.setChatRoom(room);
            systemMessage.setSender(admin);
            systemMessage.setType(MessageType.SYSTEM);
            systemMessage.setMessage("تم إنشاء المحادثة الخاصة بهذه التذكرة.");
            systemMessage.setCreatedAt(LocalDateTime.now());
            systemMessage.setReadStatus(ReadStatus.SENT);

            chatMessageRepository.save(systemMessage);
            chatService.sendSystemMessage(
                    room,
                    "تم إنشاء المحادثة"
            );

            // التذكرة
            ticket.setAssignedSupportEmployee(employee);
            ticket.setAcceptedByCustomerService(true);
            ticket.setAcceptedAt(LocalDateTime.now());
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            ticket.setUpdatedAt(LocalDateTime.now());

// الطلب
            serviceRequest.setAssignedSupportEmployee(employee);
            serviceRequest.setCurrentEmployee(employee);
            serviceRequest.setLastUpdated(LocalDateTime.now());

            requestRepository.save(serviceRequest);
            ticketRepository.save(ticket);

// إشعار بدء المحادثة
            notificationService.send(
                    ticket.getCustomer().getUser(),
                    NotificationType.SUPPORT_TICKET_ACCEPTED,
                            NotificationCategory.SUPPORT,
                    "تم استلام طلبك",
                    "قام موظف خدمة العملاء باستلام طلب الضمان ويمكنك الآن بدء المحادثة.",
                    NotificationActionType.OPEN_SECTION,
                    NotificationEntityType.SUPPORT_TICKET,
                    ticket.getId().toString(),
                    NotificationSection.CHAT
            );
            return;
        }

        // ============================
        // IN_PROGRESS -> SOLVED
        // ============================

        if (ticket.getStatus() == TicketStatus.IN_PROGRESS &&
                newStatus == TicketStatus.SOLVED) {

            if (ticket.getAssignedSupportEmployee() == null ||
                    !ticket.getAssignedSupportEmployee().getId().equals(employee.getId())) {

                throw new ApiException("ليست التذكرة الخاصة بك");
            }

            ticket.setStatus(TicketStatus.SOLVED);
            ticket.setSolvedAt(LocalDateTime.now());
            ticket.setUpdatedAt(LocalDateTime.now());

            serviceRequest.setCurrentEmployee(employee);
            serviceRequest.setLastUpdated(LocalDateTime.now());

            requestRepository.save(serviceRequest);
            ticketRepository.save(ticket);

            notificationService.send(
                    ticket.getCreatedByEmployee().getUser(),

                    NotificationType.CHAT_MESSAGE_RECEIVED,
                    NotificationCategory.CHAT,

                    "تم استلام التذكرة",
                    "قام موظف خدمة العملاء باستلام التذكرة ويمكنك الآن بدء المحادثة." ,

                    NotificationActionType.OPEN_ENTITY,
                    NotificationEntityType.CHAT,
                    ticket.getId().toString(),
                    NotificationSection.CHAT
            );

            return;
        }

        throw new ApiException("انتقال حالة غير صحيح");
    }

    public List<TicketResponseDto> searchByOrderNumber(
            String orderNumber
    ){

        return ticketRepository
                .findByRequest_OrderNumberContainingIgnoreCase(orderNumber)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<TicketListDto> getSupportTickets(
            HttpServletRequest request
    ){

        User user = authService.getAuthenticatedUser(request);

        if(user.getEmployee()==null ||
                user.getEmployee().getEmployeeRole()!=EmployeeRole.SUPPORT){

            throw new ApiException("غير مصرح");
        }

        return ticketRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toListDto)
                .toList();
    }

    public TicketListDto toListDto(Ticket ticket){

        TicketListDto dto = new TicketListDto();

        dto.setId(ticket.getId());

        dto.setTicketNumber(ticket.getTicketNumber());

        dto.setOrderNumber(ticket.getRequest().getOrderNumber());

        dto.setRequestId(ticket.getRequest().getId());

        dto.setCustomerName(
                ticket.getRequest()
                        .getCustomer()
                        .getUser()
                        .getFullName()
        );

        dto.setCarModel(
                ticket.getRequest()
                        .getCar()
                        .getModel()
                        .getNameAr()
        );

        dto.setProblemType(ticket.getProblemType());

        dto.setDescription(ticket.getDescription());

        dto.setPriority(ticket.getPriority());

        dto.setStatus(ticket.getStatus());

        dto.setAcceptedByCustomerService(
                ticket.getAcceptedByCustomerService()
        );

        dto.setCreatedAt(ticket.getCreatedAt());

        if(ticket.getAssignedSupportEmployee()!=null){

            dto.setAssignedSupportEmployeeName(
                    ticket.getAssignedSupportEmployee()
                            .getUser()
                            .getFullName()
            );
        }

        return dto;
    }

    public TicketSupportCountDto getSupportTicketCount(
            HttpServletRequest request
    ) {

        User user = authService.getAuthenticatedUser(request);

        if (user.getEmployee() == null ||
                user.getEmployee().getEmployeeRole() != EmployeeRole.SUPPORT) {

            throw new ApiException("غير مصرح");
        }

        long active =
                ticketRepository.countByStatus(TicketStatus.ACTIVE);

        long inProgress =
                ticketRepository.countByStatus(TicketStatus.IN_PROGRESS);

        long solved =
                ticketRepository.countByStatus(TicketStatus.SOLVED);

        return new TicketSupportCountDto(
                active,
                inProgress,
                solved
        );
    }




}
