package org.example.tears.OutDTO;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.example.tears.InpDTO.LocationDto;

import java.util.List;

@Data
@Setter
@Getter
public class RequestResponseDto {
    private Integer id;
    private String orderNumber;
    private String status;
    private Integer totalPrice;
    private String appointmentDate;
    private String appointmentTime;

    @NotEmpty(message = "يجب اختيار موقع واحد على الأقل")
    private List<LocationDto> locations; // optional if newLocation or locationId is set

    @Column(nullable = false)
    private boolean hydraulicTruck;

    private Integer locationId;
    private LocationDto newLocation;

    private Double lat;
    private Double lng;
    private String address;
    private String paymentMethod;
}
