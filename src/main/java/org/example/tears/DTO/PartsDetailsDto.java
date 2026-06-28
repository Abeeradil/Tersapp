package org.example.tears.DTO;

import lombok.Data;

import java.util.List;

@Data
public class PartsDetailsDto {

    private String problemDescription;

    private List<PartReportDto> parts;

    private Integer totalParts;

    private Integer totalLabor;

    private Integer grandTotal;
}