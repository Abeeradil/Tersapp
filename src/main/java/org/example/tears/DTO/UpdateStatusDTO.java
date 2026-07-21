package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.StaffRequestStatus;

@Data
public class UpdateStatusDTO {

        private StaffRequestStatus status;

        private String note;

    }
