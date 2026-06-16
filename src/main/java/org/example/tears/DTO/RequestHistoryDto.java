package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.InpDTO.LocationDto;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class RequestHistoryDto {

    private Integer id;

    private String orderNumber;

    private String serviceName;

    private LocalDate appointmentDate;

    private LocalTime appointmentTime;

    private Double totalPrice;

    private Boolean canReview;
    private Boolean reviewed;

    private String status;

    private String customerStatus;

    private String requestState;
}