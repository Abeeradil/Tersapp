package org.example.tears.Config;

import lombok.AllArgsConstructor;
import org.example.tears.Model.JwtUtil;
import org.example.tears.Model.User;
import org.example.tears.Repository.UserRepository;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtService;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message,
                              MessageChannel channel) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String auth =
                    accessor.getFirstNativeHeader("Authorization");

            if (auth != null && auth.startsWith("Bearer ")) {

                String token = auth.substring(7);

                String phone =
                        jwtService.extractUsername(token);

                User user =
                        userRepository.findByPhoneNumber(phone)
                                .orElseThrow();

                accessor.setUser(
                        new UsernamePasswordAuthenticationToken(
                                phone,
                                null,
                                List.of()
                        )
                );
            }
        }

        return message;
    }
}