package org.example.tears.InpDTO;

import lombok.Data;

@Data
public class LocationDto {
    private Integer id;
    private Double lat;
    private Double lng;
    private String address;
    private String title;
}
