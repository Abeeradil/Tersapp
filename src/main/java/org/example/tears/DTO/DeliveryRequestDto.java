package org.example.tears.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DeliveryRequestDto {

    @NotNull
    private Integer locationId;

    @NotNull
    private LocalDate deliveryDate;

    @NotNull
    private LocalTime deliveryTime;
}