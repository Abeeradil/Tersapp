package org.example.tears.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.tears.Enums.ChatStatus;

@Data
@AllArgsConstructor
public class ChatRoomResponse {

    private Integer roomId;

    private Integer ticketId;

    private ChatStatus status;
}