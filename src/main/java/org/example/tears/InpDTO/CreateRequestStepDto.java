package org.example.tears.InpDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

    private List<LocationDto> locations;

    private Integer locationId; // لو موقع محفوظ
    private LocationDto newLocation; // لو موقع جديد

    @NotBlank(message = "يجب اختيار تاريخ الموعد")
    private LocalDate appointmentDate;

    @NotBlank(message = "يجب اختيار وقت الموعد")
    private LocalTime appointmentTime; // صيغة "HH:mm"

    private String couponCode;

    private Integer estimatedPrice;
    private boolean initialPaid;
    private Integer finalPrice;
    private boolean finalPaid;
    private String paymentMethod;  // ممكن نضيف طريقة الدفع لكل مرحلة لو حابة

}