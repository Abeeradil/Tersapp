package org.example.tears.DTO;

import lombok.Data;

@Data
public class CurrentRequestDto {

    private Integer id;

    private String orderNumber;

    private String serviceName;

    private String status;

    private String requestState;
}