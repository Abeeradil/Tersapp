package org.example.tears.DTO;
import lombok.Data;

@Data
    public class EmployeeRequestResponseDto {
        private Integer id;
        private String orderNumber;

        private String status;           // NEW_REQUEST مثلاً
        private String serviceOption;    // نوع الخدمة
        private String ProblemDescription;

        //private String carBrand;         // تويوتا
        private Integer carId;
         private String carModel;         // كامري
        //private Integer carYear;         // 2022
        private String plateNumber;      // أ ب ج 1234

        private String address;

        private String appointmentDate;
        private String appointmentTime;
    }
