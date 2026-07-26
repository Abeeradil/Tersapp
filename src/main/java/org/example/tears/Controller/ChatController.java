package org.example.tears.Controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.Model.User;
import org.example.tears.Service.AuthService;
import org.example.tears.Service.ChatService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@AllArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final AuthService authService;

    @GetMapping("/{requestId}/messages")
    public ApiResponse getMessages(
            @PathVariable Integer requestId,
            HttpServletRequest request
    ) {

        User user = authService.getAuthenticatedUser(request);

        return new ApiResponse(
                true,
                "تم جلب الرسائل",
                chatService.getMessages(requestId, user)
        );
    }

    @PutMapping("/{requestId}/read")
    public ApiResponse markAsRead(
            @PathVariable Integer requestId,
            HttpServletRequest request
    ) {

        User user = authService.getAuthenticatedUser(request);

        chatService.markAsRead(requestId, user);

        return new ApiResponse(
                true,
                "تم تحديث حالة الرسائل"
        );
    }

    @GetMapping("/{requestId}/online")
    public ApiResponse isOnline(
            @PathVariable Integer requestId,
            @AuthenticationPrincipal User user
    ){

        return new ApiResponse(
                true,
                "success",
                chatService.isOtherUserOnline(
                        requestId,
                        user
                )
        );
    }
}
