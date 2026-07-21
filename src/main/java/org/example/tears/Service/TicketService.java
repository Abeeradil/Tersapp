package org.example.tears.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.CreateTicketDto;
import org.example.tears.DTO.TicketDetailsDto;
import org.example.tears.DTO.TicketResponseDto;
import org.example.tears.DTO.UpdateTicketStatusDto;
import org.example.tears.Enums.TicketStatus;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.Ticket;
import org.example.tears.Model.User;
import org.example.tears.Repository.CarServiceRequestRepository;
import org.example.tears.Repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;

    private final CarServiceRequestRepository requestRepository;

    private final AuthService authService;

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

        dto.setCarModel(
                ticket.getRequest()
                        .getCar()
                        .getModel()
                        .getNameAr()
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

        dto.setCarModel(
                ticket.getRequest()
                        .getCar()
                        .getModel()
                        .getNameAr()
        );

        dto.setProblemType(ticket.getProblemType());

        dto.setPriority(ticket.getPriority());

        dto.setStatus(ticket.getStatus());

        dto.setDescription(ticket.getDescription());

        dto.setCreatedAt(ticket.getCreatedAt());

        dto.setUpdatedAt(ticket.getUpdatedAt());

        dto.setSolvedAt(ticket.getSolvedAt());

        if (ticket.getAssignedEmployee() != null) {

            dto.setAssignedEmployee(
                    ticket.getAssignedEmployee()
                            .getUser()
                            .getFullName()
            );
        }

        return dto;
    }

    @Transactional
    public void updateTicketStatus(
            Integer ticketId,
            UpdateTicketStatusDto dto,
            HttpServletRequest request
    ) {

        User user = authService.getAuthenticatedUser(request);

        if (user.getEmployee() == null) {
            throw new ApiException("غير مصرح");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ApiException("التذكرة غير موجودة"));

        if (dto.getStatus() == TicketStatus.ACTIVE) {
            throw new ApiException("لا يمكن الرجوع إلى ACTIVE");
        }

        if (ticket.getStatus() == TicketStatus.SOLVED) {
            throw new ApiException("تم إغلاق التذكرة");
        }

        if (dto.getStatus() == TicketStatus.IN_PROGRESS) {

            if (ticket.getAssignedEmployee() != null &&
                    !ticket.getAssignedEmployee().getId().equals(user.getEmployee().getId())) {

                throw new ApiException("التذكرة محجوزة لموظف آخر");
            }
            ticket.setAcceptedByCustomerService(true);
            ticket.setAssignedEmployee(user.getEmployee());
        }

        if (dto.getStatus() == TicketStatus.SOLVED) {

            if (ticket.getAssignedEmployee() == null ||
                    !ticket.getAssignedEmployee().getId().equals(user.getEmployee().getId())) {

                throw new ApiException("يجب استلام التذكرة أولاً");
            }

            ticket.setSolvedAt(LocalDateTime.now());
        }

        ticket.setStatus(dto.getStatus());

        ticket.setUpdatedAt(LocalDateTime.now());

        ticketRepository.save(ticket);
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


}
