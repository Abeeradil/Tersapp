package org.example.tears.DTO;

import lombok.Data;

@Data
public class RequestDetailsDto {


    private Integer id;
    private String orderNumber;

    private String serviceName;

    private String customerStatus;

    private String requestState; // ACTIVE / COMPLETED / CANCELLED

    private String plateNumberArabic;
    private String plateNumberEnglish;

    private Double totalPrice;

    private String address;

    private String requestState;
}
