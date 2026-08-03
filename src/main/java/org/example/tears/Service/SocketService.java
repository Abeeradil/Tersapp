package org.example.tears.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void send(String topic, Object body) {
        messagingTemplate.convertAndSend(topic, body);
    }

}
