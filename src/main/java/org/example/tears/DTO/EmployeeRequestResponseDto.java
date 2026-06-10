package org.example.tears.DTO;
import lombok.Data;
import org.example.tears.Model.CarModel;

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

        private Integer carId;
         private CarModel carModel;         // كامري
        private String address;

        private String plateNumberArabic;
        private String plateNumberEnglish;

        private LocalDateTime createdAt;

    }
