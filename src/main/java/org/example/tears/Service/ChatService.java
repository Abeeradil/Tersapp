package org.example.tears.Service;

import lombok.AllArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.ChatMessageResponse;
import org.example.tears.DTO.SendMessageDto;
import org.example.tears.DTO.UploadResponse;
import org.example.tears.Enums.ReadStatus;
import org.example.tears.Enums.UserRole;
import org.example.tears.Model.CarServiceRequest;
import org.example.tears.Model.ChatMessage;
import org.example.tears.Model.ChatRoom;
import org.example.tears.Model.User;
import org.example.tears.Repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.example.tears.Enums.ChatStatus.OPEN;



@Service
@AllArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepo;
    private final PresenceService presenceService;
    private final MessageStatusRepository messageStatusRepository;

    private final CarServiceRequestRepository requestRepository;

    @Transactional
    public ChatRoom createRoomIfNotExists(
            CarServiceRequest request
    ) {

        return chatRoomRepository
                .findByRequest(request)
                .orElseGet(() -> {

                    ChatRoom room = new ChatRoom();

                    room.setRequest(request);

                    room.setCreatedAt(LocalDateTime.now());

                   room.setStatus(OPEN);

                    return chatRoomRepository.save(room);
                });
    }


    public ChatRoom getRoom(
            Integer requestId,
            User user
    ) {

        CarServiceRequest request =
                requestRepository.findById(requestId)
                        .orElseThrow(() ->
                                new ApiException("الطلب غير موجود"));

        validateUserAccess(request, user);

        return chatRoomRepository
                .findByRequest(request)
                .orElseThrow(() ->
                        new ApiException("لا توجد محادثة"));
    }

    public List<ChatMessage> getMessages(
            Integer requestId,
            User user
    ) {

        ChatRoom room = getRoom(
                requestId,
                user
        );

        return chatMessageRepository
                .findByChatRoomOrderByCreatedAtAsc(room);
    }


    public Page<ChatMessage> getMessagesPage(
            Integer requestId,
            User user,
            Pageable pageable
    ){

        ChatRoom room = getRoom(requestId, user);

        return chatMessageRepository
                .findByChatRoomOrderByCreatedAtDesc(
                        room,
                        pageable
                );
    }

    private void validateUserAccess(
            CarServiceRequest request,
            User user
    ) {

        // الأدمن
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }

        // العميل
        if (user.getCustomer() != null &&
                request.getCustomer().getId().equals(user.getCustomer().getId())) {
            return;
        }

        // الموظف
        if (user.getEmployee() != null) {

            Integer employeeId = user.getEmployee().getId();

            if ((request.getAssignedEmployee() != null &&
                    request.getAssignedEmployee().getId().equals(employeeId))

                    ||

                    (request.getAssignedPricingEmployee() != null &&
                            request.getAssignedPricingEmployee().getId().equals(employeeId))

                    ||

                    (request.getCurrentEmployee() != null &&
                            request.getCurrentEmployee().getId().equals(employeeId))) {

                return;
            }
        }

        throw new ApiException("غير مصرح لك بالدخول لهذه المحادثة");
    }

    @Transactional
    public void sendMessage(
            SendMessageDto dto,
            String phone
    ) {

        User sender = userRepo
                .findByPhoneNumber(phone)
                .orElseThrow(() ->
                        new ApiException("المستخدم غير موجود"));

        ChatRoom room = getRoom(
                dto.getRequestId(),
                sender
        );

        ChatMessage message = new ChatMessage();

        message.setChatRoom(room);
        message.setSender(sender);
        message.setType(dto.getType());
        message.setMessage(dto.getMessage());
        message.setFileUrl(dto.getFileUrl());
        message.setFileName(dto.getFileName());
        message.setFileSize(dto.getFileSize());
        message.setVoiceDuration(dto.getVoiceDuration());
        message.setCreatedAt(LocalDateTime.now());

        chatMessageRepository.save(message);

        messagingTemplate.convertAndSend(
                "/topic/chat/" + room.getId(),
                mapToResponse(message, sender)
        );
    }

    private ChatMessageResponse mapToResponse(
            ChatMessage message,
            User currentUser
    ) {

        ChatMessageResponse dto = new ChatMessageResponse();

        dto.setId(message.getId());

        dto.setSenderId(message.getSender().getId());

        dto.setSenderName(
                message.getSender().getFullName()
        );

        dto.setType(message.getType());

        dto.setMessage(message.getMessage());

        dto.setFileUrl(message.getFileUrl());

        dto.setFileName(message.getFileName());

        dto.setFileSize(message.getFileSize());

        dto.setVoiceDuration(message.getVoiceDuration());

        dto.setCreatedAt(message.getCreatedAt());

        dto.setMine(
                message.getSender().getId().equals(currentUser.getId())
        );

        return dto;
    }

    @Transactional
    public void markAsRead(
            Integer requestId,
            User currentUser
    ){

        ChatRoom room =
                getRoom(requestId,currentUser);

        List<ChatMessage> messages =
                chatMessageRepository
                        .findByChatRoomOrderByCreatedAtAsc(room);

        for(ChatMessage message : messages){

            if(message.getSender().getId()
                    .equals(currentUser.getId())){

                continue;
            }

            if(message.getReadStatus()==ReadStatus.READ){

                continue;
            }

            message.setReadStatus(ReadStatus.READ);

            chatMessageRepository.save(message);
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + room.getId() + "/read",
                    message.getId()
            );
        }
    }

    public Boolean isOtherUserOnline(
            Integer requestId,
            User currentUser
    ){

        CarServiceRequest request =
                requestRepository.findById(requestId)
                        .orElseThrow(() ->
                                new ApiException("الطلب غير موجود"));

        validateUserAccess(request, currentUser);

        User other;

        if(currentUser.getCustomer()!=null){

            other =
                    request.getAssignedEmployee()
                            .getUser();

        }else{

            other =
                    request.getCustomer()
                            .getUser();
        }

        return presenceService.isOnline(
                other.getPhoneNumber()
        );
    }

    public UploadResponse upload(MultipartFile file) {

        try {

            String name = UUID.randomUUID() + "_" + file.getOriginalFilename();

            Path path = Paths.get("uploads", name);

            Files.createDirectories(path.getParent());

            Files.write(path, file.getBytes());

            return new UploadResponse(
                    "/uploads/" + name,
                    file.getOriginalFilename(),
                    file.getSize()
            );

        } catch (IOException e) {

            throw new ApiException("فشل رفع الملف");

        }
    }


//    uploadFile()
//
//    deleteMessage()
}
