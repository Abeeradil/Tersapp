package org.example.tears.DTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class EmployeeRequestResponseDto {

    private Integer id;
    private String orderNumber;

    private String requestState;
    private String serviceOption;
    private String problemDescription;

    private String status;

    private String carModelName;
    private String carModelNameAr;

    private String address;

    private String plateNumberArabic;
    private String plateNumberEnglish;

    private List<RequestNoteDTO> notes;

    private Boolean warrantyEligible;
    private Boolean warrantyRequest;
    private String warrantyStatus;
    private Integer warrantyRequestId;
    private String requestType;
    private String warrantyDescription;

    private boolean employeeImagesReceived;
    private boolean employeeReportReceived;

    private LocalDateTime createdAt;
}