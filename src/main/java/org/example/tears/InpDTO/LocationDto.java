package org.example.tears.InpDTO;

import lombok.Data;

@Data
public class LocationDto {
    private Double lat;
    private Double lng;
    private String address;
    private String title;
}
