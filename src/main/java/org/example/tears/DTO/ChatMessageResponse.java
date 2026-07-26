package org.example.tears.DTO;

import lombok.Data;
import org.example.tears.Enums.MessageType;
import org.example.tears.Enums.ReadStatus;

import java.time.LocalDateTime;

@Data
public class ChatMessageResponse {

    private Integer id;

    private Integer senderId;

    private String senderName;

    private MessageType type;

    private String message;

    private String fileUrl;

    private String fileName;

    private Long fileSize;
    private Boolean mine;

    private Integer voiceDuration;

    private LocalDateTime createdAt;

    private ReadStatus status;
}