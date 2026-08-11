package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.WarrantyReason;

@Data
public class WarrantyRequestDto {

    private WarrantyReason warrantyReason;

    private String description;
}