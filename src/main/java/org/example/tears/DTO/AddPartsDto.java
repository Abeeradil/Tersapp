package org.example.tears.DTO;

import lombok.Data;

import java.util.List;
@Data
public class AddPartsDto {
    private String problemDescription;
    private List<PartDto> parts;

}
