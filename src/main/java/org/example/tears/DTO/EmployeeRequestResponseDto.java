package org.example.tears.DTO;
import lombok.Data;

import java.time.LocalDateTime;


@Data
    public class EmployeeRequestResponseDto {
        private Integer id;
        private String orderNumber;

        private String status;           // NEW_REQUEST مثلاً
        private String requestState;
        private String serviceOption;    // نوع الخدمة
        private String ProblemDescription;

        private String carModelName;
        private String carModelNameAr; // كامري

        private String address;

        private String plateNumberArabic;
        private String plateNumberEnglish;

    private Boolean warrantyEligible;
    private Boolean warrantyRequest;
    private String warrantyStatus;
    private String warrantyDescription;


        private LocalDateTime createdAt;

    }
