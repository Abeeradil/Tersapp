package org.example.tears.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerPartDto {

    @NotNull
    private Integer partId;

    @NotNull
    @Min(1)
    private Integer quantity;

}