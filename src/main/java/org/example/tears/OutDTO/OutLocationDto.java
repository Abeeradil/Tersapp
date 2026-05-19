package org.example.tears.OutDTO;

import lombok.Data;

@Data
public class OutLocationDto {
    private Integer id;

    private Double lat;

    private Double lng;

    private String address;

    private String title;
}
