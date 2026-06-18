package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.StaffRequestStatus;

@Data
public class UpdateStatusDTO {

        private StaffRequestStatus status;

        private String note;

        private String imageUrl; // فقط إذا فيه صورة (RECEIVED مثلاً)

    }
