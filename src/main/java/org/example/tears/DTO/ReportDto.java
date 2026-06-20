package org.example.tears.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReportDto {

    private String Content;
    private String fileUrl;
    private String description;
    private LocalDateTime CreatedAt;
}
