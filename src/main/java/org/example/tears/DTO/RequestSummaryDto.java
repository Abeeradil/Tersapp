package org.example.tears.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RequestSummaryDto {
    private Integer id;
    private String orderNumber;
    private String status;
    private String paymentStatus;
    private Integer totalPrice;

    private EmployeeSummaryDto assignedEmployee; // 👈 هنا فقط
}