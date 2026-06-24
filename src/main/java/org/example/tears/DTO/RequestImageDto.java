package org.example.tears.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequestImageDto {

    private Integer id;

    private String imageUrl;

    private String status;

    private LocalDateTime uploadedAt;
}