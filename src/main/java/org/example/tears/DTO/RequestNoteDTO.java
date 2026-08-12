package org.example.tears.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequestNoteDTO {

        private Integer id;

        private String note;

        private String type;

        private Integer employeeId;

        private String employeeName;

        private LocalDateTime createdAt;
    }
