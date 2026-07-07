package org.example.tears.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.tears.Enums.PricingStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PricingTimelineDto {

    private String title;

    private PricingStatus status;

    private LocalDateTime date;

    private boolean completed;

    private boolean current;
}