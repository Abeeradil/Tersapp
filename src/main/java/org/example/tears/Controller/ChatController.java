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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/chat")
@AllArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final AuthService authService;

    @GetMapping("/{ticketId}/room")
    public ApiResponse room(
            @PathVariable Integer ticketId,
            HttpServletRequest request
    ) {

        User user = authService.getAuthenticatedUser(request);

        ChatRoom room = chatService.getRoom(ticketId, user);

        return new ApiResponse(
                true,
                "success",
                new ChatRoomResponse(
                        room.getId(),
                        room.getTicket().getId(),
                        room.getStatus()
                )
        );
    }

    @PostMapping("/upload")
    public ApiResponse upload(
            @RequestParam MultipartFile file
    ) throws IOException {

        UploadResponse response = chatService.upload(file);

        return new ApiResponse(
                true,
                "تم رفع الملف",
                response
        );
    }

    @GetMapping("/{ticketId}/messages")
    public ApiResponse getMessages(
            @PathVariable Integer ticketId,
            HttpServletRequest request
    ) {

        User user = authService.getAuthenticatedUser(request);

        return new ApiResponse(
                true,
                "تم جلب الرسائل",
                chatService.getMessages(ticketId, user)
        );
    }

    @PutMapping("/{ticketId}/read")
    public ApiResponse markAsRead(
            @PathVariable Integer ticketId,
            HttpServletRequest request
    ) {

        User user = authService.getAuthenticatedUser(request);

        chatService.markAsRead(ticketId, user);

        return new ApiResponse(
                true,
                "تم تحديث حالة الرسائل"
        );
    }

    @GetMapping("/{ticketId}/online")
    public ApiResponse isOnline(
            @PathVariable Integer ticketId,
            HttpServletRequest request
    ) {

        User user = authService.getAuthenticatedUser(request);

        return new ApiResponse(
                true,
                "success",
                chatService.isOtherUserOnline(
                        ticketId,
                        user
                )
        );
    }
}