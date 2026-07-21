package org.example.tears.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.CreateTicketDto;
import org.example.tears.DTO.TicketResponseDto;
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


}
