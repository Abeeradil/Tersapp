package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.StaffRequestStatus;

@Data
public class UpdateStatusDTO {
    private StaffRequestStatus status; // مو String ❗
    private String note;     // ملاحظة اختيارية
}
