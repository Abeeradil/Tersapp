package org.example.tears.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequestNoteDTO {

    private String note;

    private String employeeName;

    private String step;

    private LocalDateTime createdAt;

}
