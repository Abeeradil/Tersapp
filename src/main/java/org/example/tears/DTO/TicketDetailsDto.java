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

    private String serviceOption;

    private Integer requestId;

    private String orderNumber;

    private String customerName;

    private String customerPhone;

    private String carModel;

    private String city;

    private TicketProblemType problemType;

    private TicketPriority priority;

    private TicketStatus status;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String address;

    private String plateArabic;
    private String plateEnglish;

    private Boolean acceptedByCustomerService;
    
    private LocalDateTime acceptedAt;

    private LocalDateTime solvedAt;

    private String assignedEmployeeName;

    private String assignedEmployeePhone;


    private String assignedEmployee;

}