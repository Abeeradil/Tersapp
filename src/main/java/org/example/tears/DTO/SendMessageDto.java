package org.example.tears.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.tears.Enums.MessageType;

@Data
public class SendMessageDto {

    @NotNull
    private Integer requestId;

    @NotNull
    private MessageType type;

    private String message;

    private String fileUrl;

    private String fileName;

    private Long fileSize;

    private Integer voiceDuration;
}