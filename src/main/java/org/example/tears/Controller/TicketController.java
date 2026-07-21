package org.example.tears.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.DTO.CreateTicketDto;
import org.example.tears.DTO.UpdateTicketStatusDto;
import org.example.tears.Service.TicketService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tears/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/creat")
    public ApiResponse createTicket(
            @Valid @RequestBody CreateTicketDto dto,
            HttpServletRequest request
    ) {

        return new ApiResponse(
                true,
                "تم إنشاء التذكرة",
                ticketService.createTicket(request, dto)
        );
    }

    @GetMapping("/my")
    public ApiResponse myTickets(
            HttpServletRequest request
    ){

        return new ApiResponse(
                true,
                "تم جلب التذاكر",
                ticketService.getMyTickets(request)
        );

    }

    @GetMapping("/details/{ticketId}")
    public ApiResponse getTicketDetails(
            @PathVariable Integer ticketId
    ){

        return new ApiResponse(
                true,
                "تم جلب التذكرة",
                ticketService.getTicketDetails(ticketId)
        );

    }

    @PutMapping("/{ticketId}/status")
    public ApiResponse updateStatus(
            @PathVariable Integer ticketId,
            @RequestBody @Valid UpdateTicketStatusDto dto,
            HttpServletRequest request
    ){

        ticketService.updateTicketStatus(
                ticketId,
                dto,
                request
        );

        return new ApiResponse(
                true,
                "تم تحديث حالة التذكرة"
        );
    }

    @GetMapping("/search")
    public ApiResponse search(
            @RequestParam String orderNumber
    ){

        return new ApiResponse(
                true,
                "تم جلب النتائج",
                ticketService.searchByOrderNumber(orderNumber)
        );

    }

}