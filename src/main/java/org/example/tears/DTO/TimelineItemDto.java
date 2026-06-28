package org.example.tears.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tears.Enums.StaffRequestStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimelineItemDto {
    private String title;
    private StaffRequestStatus status;
    private LocalDateTime time;
    private boolean completed;
}