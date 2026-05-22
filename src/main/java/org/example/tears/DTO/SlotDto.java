package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.AppointmentSlotStatus;

import java.time.LocalTime;

@Data
public class SlotDto {
        private LocalTime time;
        private AppointmentSlotStatus status;
    }