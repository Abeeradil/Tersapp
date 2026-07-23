package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.WarrantyProblemType;

@Data
public class WarrantyRequestDto {

    private WarrantyProblemType problemType;

    private String description;

}