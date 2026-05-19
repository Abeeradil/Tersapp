package org.example.tears.OutDTO;

import lombok.Data;

import java.util.List;

@Data
public class WorkingHoursResponseDto {

    private List<String> availableTimes;

    private List<String> supportedCities;

    private String workingDays;

    private String note;

}
