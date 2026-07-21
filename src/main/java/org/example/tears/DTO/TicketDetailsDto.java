package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.TicketPriority;
import org.example.tears.Enums.TicketProblemType;
import org.example.tears.Enums.TicketStatus;

import java.time.LocalDateTime;

@Data
public class TicketDetailsDto {

    private Integer id;

    private String ticketNumber;

    private Integer requestId;

    private String orderNumber;

    private String customerName;

    private String customerPhone;

    private String carModel;

    private TicketProblemType problemType;

    private TicketPriority priority;

    private TicketStatus status;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime solvedAt;

    private String assignedEmployee;

}