package org.example.tears.Service;

import lombok.AllArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.ChatMessageResponse;
import org.example.tears.DTO.SendMessageDto;
import org.example.tears.DTO.UploadResponse;
import org.example.tears.Enums.ChatStatus;
import org.example.tears.Enums.MessageType;
import org.example.tears.Enums.ReadStatus;
import org.example.tears.Enums.UserRole;
import org.example.tears.Model.*;
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

import static org.example.tears.Enums.ReadStatus.SENT;


@Service
@AllArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final TicketRepository ticketRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private final UserRepository userRepo;
    private final PresenceService presenceService;


    @Transactional
    public ChatRoom createRoomIfNotExists(
            Ticket ticket
    ) {

        return chatRoomRepository
                .findByTicket(ticket)
                .orElseGet(() -> {

                    ChatRoom room = new ChatRoom();

                    room.setTicket(ticket);

                    room.setCreatedAt(LocalDateTime.now());

                    room.setStatus(ChatStatus.OPEN);

                    return chatRoomRepository.save(room);
                });
    }


    public ChatRoom getRoom(Integer ticketId, User user) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ApiException("التذكرة غير موجودة"));

        validateUserAccess(ticket, user);

        return chatRoomRepository
                .findByTicket(ticket)
                .orElseThrow(() ->
                        new ApiException("لا توجد محادثة"));
    }

    public List<ChatMessageResponse> getMessages(
            Integer ticketId,
            User user
    ) {

        ChatRoom room = getRoom(ticketId, user);

        return chatMessageRepository
                .findByChatRoomOrderByCreatedAtAsc(room)
                .stream()
                .map(message -> mapToResponse(message, user))
                .toList();
    }


    public Page<ChatMessage> getMessagesPage(
            Integer ticketId,
            User user,
            Pageable pageable
    ){

        ChatRoom room = getRoom(ticketId,user);

        return chatMessageRepository
                .findByChatRoomOrderByCreatedAtDesc(
                        room,
                        pageable
                );
    }

    private void validateUserAccess(
            Ticket ticket,
            User user
    ) {

        // الأدمن
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }

        // العميل (إذا مستقبلاً صار في محادثة عميل)
        if (user.getCustomer() != null &&
                ticket.getCustomer() != null &&
                ticket.getCustomer().getId().equals(user.getCustomer().getId())) {
            return;
        }

        // الموظف
        if (user.getEmployee() != null) {

            Integer employeeId = user.getEmployee().getId();

            System.out.println("========== CHAT AUTH ==========");
            System.out.println("Logged Employee = " + employeeId);

            System.out.println("Ticket CreatedByEmployee = "
                    + (ticket.getCreatedByEmployee() == null
                    ? null
                    : ticket.getCreatedByEmployee().getId()));

            System.out.println("Ticket AssignedEmployee = "
                    + (ticket.getAssignedEmployee() == null
                    ? null
                    : ticket.getAssignedEmployee().getId()));

            // موظف الصيانة (منشئ التذكرة)
            if (ticket.getCreatedByEmployee() != null &&
                    ticket.getCreatedByEmployee().getId().equals(employeeId)) {
                return;
            }

            // موظف خدمة العملاء
            if (ticket.getAssignedEmployee() != null &&
                    ticket.getAssignedEmployee().getId().equals(employeeId)) {
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

        System.out.println("========== SEND MESSAGE ==========");
        System.out.println("Phone = " + phone);
        System.out.println("TicketId = " + dto.getTicketId());
        System.out.println("Type = " + dto.getType());
        System.out.println("Message = " + dto.getMessage());

        User sender = userRepo
                .findByPhoneNumber(phone)
                .orElseThrow(() ->
                        new ApiException("المستخدم غير موجود"));

        System.out.println("Sender = " + sender.getId());

        ChatRoom room = getRoom(
                dto.getTicketId(),
                sender
        );

        System.out.println("Room = " + room.getId());

        ChatMessage message = new ChatMessage();

        message.setChatRoom(room);
        message.setSender(sender);
        message.setType(dto.getType());
        message.setMessage(dto.getMessage());
        message.setFileUrl(dto.getFileUrl());
        message.setFileName(dto.getFileName());
        message.setFileSize(dto.getFileSize());
        message.setReadStatus(ReadStatus.SENT);
        message.setVoiceDuration(dto.getVoiceDuration());
        message.setCreatedAt(LocalDateTime.now());

        chatMessageRepository.save(message);

        System.out.println("Saved Message Id = " + message.getId());

        messagingTemplate.convertAndSend(
                "/topic/chat/" + room.getId(),
                mapToResponse(message, sender)
        );

        System.out.println("Broadcast -> /topic/chat/" + room.getId());

        System.out.println("========== DONE ==========");
    }

    private ChatMessageResponse mapToResponse(
            ChatMessage message,
            User currentUser
    ) {

        ChatMessageResponse dto = new ChatMessageResponse();

        dto.setId(message.getId());

        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getFullName());

        dto.setType(message.getType());
        dto.setMessage(message.getMessage());

        dto.setStatus(message.getReadStatus());

        dto.setMine(
                currentUser != null &&
                        message.getSender() != null &&
                        message.getSender().getId().equals(currentUser.getId())
        );

        dto.setFileUrl(message.getFileUrl());
        dto.setFileName(message.getFileName());
        dto.setFileSize(message.getFileSize());
        dto.setVoiceDuration(message.getVoiceDuration());

        dto.setCreatedAt(message.getCreatedAt());

        return dto;
    }

    public void markAsRead(
            Integer ticketId,
            User currentUser
    ){

        ChatRoom room =
                getRoom(ticketId,currentUser);

        List<ChatMessage> messages =
                chatMessageRepository
                        .findByChatRoomOrderByCreatedAtAsc(room);

        for(ChatMessage message : messages){

            if(message.getSender().getId()
                    .equals(currentUser.getId()))
                continue;

            if(message.getReadStatus()==ReadStatus.READ)
                continue;

            message.setReadStatus(ReadStatus.READ);

            chatMessageRepository.save(message);

            messagingTemplate.convertAndSend(
                    "/topic/chat/"+room.getId()+"/read",
                    message.getId()
            );
        }
    }


    public Boolean isOtherUserOnline(
            Integer ticketId,
            User currentUser
    ){

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ApiException("التذكرة غير موجودة"));

        validateUserAccess(ticket,currentUser);

        User other;

        if(currentUser.getCustomer()!=null){

            other = ticket.getAssignedEmployee().getUser();

        }else{

            if(ticket.getCreatedByEmployee()
                    .getId()
                    .equals(currentUser.getEmployee().getId())){

                other = ticket.getAssignedEmployee().getUser();

            }else{

                other = ticket.getCreatedByEmployee().getUser();
            }
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

            System.out.println("Saved to: " + path.toAbsolutePath());
            System.out.println("Exists: " + Files.exists(path));

            String url =
                    "https://tersapp-production.up.railway.app/uploads/" + name;

            return new UploadResponse(
                    url,
                    file.getOriginalFilename(),
                    file.getSize()
            );

        } catch (IOException e) {

            throw new ApiException("فشل رفع الملف");

        }
    }

    @Transactional
    public void sendSystemMessage(ChatRoom room, String text) {

        User admin = userRepo.findById(8)
                .orElseThrow(() -> new ApiException("Admin غير موجود"));

        ChatMessage message = new ChatMessage();
        message.setChatRoom(room);
        message.setSender(admin);
        message.setType(MessageType.SYSTEM);
        message.setMessage(text);
        message.setCreatedAt(LocalDateTime.now());
        message.setReadStatus(SENT);

        chatMessageRepository.save(message);

        messagingTemplate.convertAndSend(
                "/topic/chat/" + room.getId(),
                mapToResponse(message, admin)
        );
    }


    //    deleteMessage()
}
