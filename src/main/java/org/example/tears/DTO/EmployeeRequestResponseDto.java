package org.example.tears.DTO;
import lombok.Data;
import org.example.tears.InpDTO.LocationDto;
import org.example.tears.Model.CarModel;
import org.example.tears.Model.Location;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
    public class EmployeeRequestResponseDto {
        private Integer id;
        private String orderNumber;

        private String status;           // NEW_REQUEST مثلاً
        private String serviceOption;    // نوع الخدمة
        private String ProblemDescription;

        private String carModelName;
        private String carModelNameAr; // كامري

        private LocationDto location;
        private String plateNumberArabic;
        private String plateNumberEnglish;

        private LocalDateTime createdAt;

    }
