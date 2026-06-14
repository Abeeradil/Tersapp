package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.InpDTO.LocationDto;
import org.example.tears.Model.Location;

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

    private LocationDto location;

}
