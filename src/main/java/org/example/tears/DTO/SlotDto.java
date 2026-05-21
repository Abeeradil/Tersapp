package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.AppointmentSlotStatus;

@Data
public class SlotDto {
        private String time;
        private AppointmentSlotStatus status;
    }