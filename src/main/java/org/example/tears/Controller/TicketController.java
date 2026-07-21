package org.example.tears.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.DTO.CreateTicketDto;
import org.example.tears.Service.TicketService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}