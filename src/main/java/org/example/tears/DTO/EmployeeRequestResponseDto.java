package org.example.tears.DTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;


@Data
    public class EmployeeRequestResponseDto {
        private Integer id;
        private String orderNumber;

        private String requestState;
        private String serviceOption;    // نوع الخدمة
        private String ProblemDescription;

        private String status;

        private String carModelName;
        private String carModelNameAr; // كامري

        private String address;

        private String plateNumberArabic;
        private String plateNumberEnglish;

    private List<RequestNoteDTO> notes;


    private Boolean warrantyEligible;
    private Boolean warrantyRequest;
    private String warrantyStatus;
    private String warrantyDescription;

    private boolean employeeImagesReceived;

    private boolean employeeReportReceived;


        private LocalDateTime createdAt;

    }
