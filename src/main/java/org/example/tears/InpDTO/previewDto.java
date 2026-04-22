package org.example.tears.InpDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class previewDto {
    private Integer carId;

    @NotBlank(message = "يجب اختيار نوع الخدمة")
    private String serviceOption;

    private boolean hydraulicTruck;

}
