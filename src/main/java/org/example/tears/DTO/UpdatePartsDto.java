package org.example.tears.DTO;

import lombok.Data;

import java.util.List;
@Data
public class UpdatePartsDto {
    private List<PartDto> parts;

    private Integer laborCost; // أجور العمل

}
