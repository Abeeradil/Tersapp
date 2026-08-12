package org.example.tears.DTO;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import org.example.tears.Enums.StaffRequestStatus;

import java.time.LocalDateTime;

@Data
public class RequestNoteDTO {

        private Integer id;

        private String note;

        private String type;

        private Integer employeeId;

        @Enumerated(EnumType.STRING)
        private StaffRequestStatus requestStatus;

        private String employeeName;

        private LocalDateTime createdAt;
    }
