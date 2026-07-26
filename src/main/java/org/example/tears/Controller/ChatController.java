package org.example.tears.Controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.example.tears.Api.ApiResponse;
import org.example.tears.DTO.ChatRoomResponse;
import org.example.tears.DTO.UploadResponse;
import org.example.tears.Model.ChatRoom;
import org.example.tears.Model.User;
import org.example.tears.Service.AuthService;
import org.example.tears.Service.ChatService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/chat")
@AllArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final AuthService authService;

    @GetMapping("/{requestId}/room")
    public ApiResponse room(
            @PathVariable Integer requestId,
            HttpServletRequest request
    ){

        User user =
                authService.getAuthenticatedUser(request);

        ChatRoom room =
                chatService.getRoom(requestId, user);

        return new ApiResponse(
                true,
                "success",
                new ChatRoomResponse(
                        room.getId(),
                        room.getRequest().getId(),
                        room.getStatus()
                )
        );
    }
    @PostMapping("/upload")
    public ApiResponse upload(
            @RequestParam MultipartFile file
    ) throws IOException {

        UploadResponse response = chatService.upload(file);

        return new ApiResponse(true, "تم رفع الملف", response);
    }

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
