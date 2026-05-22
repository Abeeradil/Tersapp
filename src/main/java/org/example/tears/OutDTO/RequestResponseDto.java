package org.example.tears.OutDTO;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.example.tears.InpDTO.LocationDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Setter
@Getter
public class RequestResponseDto {

    private Integer id;
    private String orderNumber;
    private String status;
    private Integer totalPrice;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;

    private LocationDto location;

    @Column(nullable = false)
    private boolean hydraulicTruck;
    private String paymentMethod;
}
