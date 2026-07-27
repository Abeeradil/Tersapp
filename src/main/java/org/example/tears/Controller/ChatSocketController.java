package org.example.tears.Controller;


import lombok.AllArgsConstructor;
import org.example.tears.DTO.SendMessageDto;
import org.example.tears.Service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@AllArgsConstructor
public class ChatSocketController {

    private final ChatService chatService;

    @MessageMapping("/chat.send")
    public void send(
            @Payload SendMessageDto dto,
            Principal principal
    ) {

        System.out.println("========== SOCKET ==========");
        System.out.println("Principal = "
                + (principal == null ? "NULL" : principal.getName()));

        System.out.println("TicketId = " + dto.getTicketId());

        chatService.sendMessage(dto, principal.getName());
    }



}