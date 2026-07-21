package org.example.tears.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.tears.Enums.TicketStatus;

@Data
public class UpdateTicketStatusDto {

    @NotNull
    private TicketStatus status;

}