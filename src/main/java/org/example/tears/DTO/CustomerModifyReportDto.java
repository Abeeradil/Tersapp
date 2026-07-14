package org.example.tears.DTO;

import lombok.Data;

import java.util.List;

@Data
public class CustomerModifyReportDto {

    private List<CustomerPartDto> parts;

    private String note;

}
