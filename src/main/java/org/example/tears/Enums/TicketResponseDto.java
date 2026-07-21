package org.example.tears.Enums;

import java.time.LocalDateTime;

public class TicketResponseDto {

    private Integer id;

    private String ticketNumber;

    private String orderNumber;

    private String carModel;

    private TicketProblemType problemType;

    private TicketPriority priority;

    private TicketStatus status;

    private LocalDateTime createdAt;

}