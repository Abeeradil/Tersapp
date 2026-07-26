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

        chatService.sendMessage(dto, principal.getName());

    }



}