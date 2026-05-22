package org.example.tears.InpDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class CreateRequestStepDto {

                private Integer carId;

                @NotBlank(message = "يجب اختيار نوع الخدمة")
                private String serviceOption;

                private boolean hydraulicTruck;

                @NotBlank(message = "وصف المشكلة إلزامي")
                private String problemDescription;

                private Integer locationId;

                private LocalDate appointmentDate;

                private LocalTime appointmentTime;

                private String couponCode;

                private String paymentMethod;
        }