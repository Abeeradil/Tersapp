package org.example.tears.Service;

import lombok.AllArgsConstructor;
import org.example.tears.Api.ApiException;
import org.example.tears.DTO.*;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.example.tears.Enums.ReadStatus.SENT;


@Service
@AllArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final TicketRepository ticketRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final FileStorageService fileStorageService;

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

            System.out.println("Ticket AssignedSupportEmployee = "
                    + (ticket.getAssignedSupportEmployee() == null
                    ? null
                    : ticket.getAssignedSupportEmployee().getId()));

            // موظف الصيانة (منشئ التذكرة)
            if (ticket.getCreatedByEmployee() != null &&
                    ticket.getCreatedByEmployee().getId().equals(employeeId)) {
                return;
            }

            // موظف خدمة العملاء
            if (ticket.getAssignedSupportEmployee() != null &&
                    ticket.getAssignedSupportEmployee().getId().equals(employeeId)) {
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

        long totalStart = System.currentTimeMillis();

        System.out.println("========== SEND MESSAGE ==========");
        System.out.println("Phone = " + phone);
        System.out.println("TicketId = " + dto.getTicketId());
        System.out.println("Type = " + dto.getType());
        System.out.println("Message = " + dto.getMessage());

        long start = System.currentTimeMillis();

        User sender = userRepo
                .findByPhoneNumber(phone)
                .orElseThrow(() ->
                        new ApiException("المستخدم غير موجود"));

        System.out.println("✔ findByPhoneNumber = "
                + (System.currentTimeMillis() - start) + " ms");

        start = System.currentTimeMillis();

        ChatRoom room = getRoom(
                dto.getTicketId(),
                sender
        );

        System.out.println("✔ getRoom = "
                + (System.currentTimeMillis() - start) + " ms");

        start = System.currentTimeMillis();

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

        System.out.println("✔ build message = "
                + (System.currentTimeMillis() - start) + " ms");

        start = System.currentTimeMillis();

        chatMessageRepository.save(message);

        System.out.println("✔ save message = "
                + (System.currentTimeMillis() - start) + " ms");

        start = System.currentTimeMillis();

        ChatMessageResponse response =
                mapToResponse(message, sender);

        System.out.println("✔ mapToResponse = "
                + (System.currentTimeMillis() - start) + " ms");

        start = System.currentTimeMillis();

        messagingTemplate.convertAndSend(
                "/topic/chat/" + room.getId(),
                response
        );

        System.out.println("✔ websocket broadcast = "
                + (System.currentTimeMillis() - start) + " ms");

        System.out.println("========== TOTAL TIME = "
                + (System.currentTimeMillis() - totalStart)
                + " ms ==========");
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

            other = ticket.getAssignedSupportEmployee().getUser();

        }else{

            if(ticket.getCreatedByEmployee()
                    .getId()
                    .equals(currentUser.getEmployee().getId())){

                other = ticket.getAssignedSupportEmployee().getUser();

            }else{

                other = ticket.getCreatedByEmployee().getUser();
            }
        }

        return presenceService.isOnline(
                other.getPhoneNumber()
        );
    }

    public UploadResponse upload(MultipartFile file) {

        String fileUrl =
                fileStorageService.saveFile(file, "chat");

        return new UploadResponse(
                fileUrl,
                file.getOriginalFilename(),
                file.getSize()
        );
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
    public void sendTyping(
            TypingDto dto,
            String phone
    ) {

        User sender = userRepo.findByPhoneNumber(phone)
                .orElseThrow(() -> new ApiException("المستخدم غير موجود"));

        ChatRoom room = getRoom(dto.getTicketId(), sender);

        messagingTemplate.convertAndSend(
                "/topic/chat/" + room.getId() + "/typing",
                Map.of(
                        "userId", sender.getId(),
                        "userName", sender.getFullName(),
                        "typing", dto.getTyping()
                )
        );
    }

    @Transactional
    public void deleteMessage(
            DeleteMessageDto dto,
            String phone
    ) {

        User user = userRepo.findByPhoneNumber(phone)
                .orElseThrow(() -> new ApiException("المستخدم غير موجود"));

        ChatMessage message = chatMessageRepository.findById(dto.getMessageId())
                .orElseThrow(() -> new ApiException("الرسالة غير موجودة"));

        // فقط صاحب الرسالة أو الأدمن
        if (!message.getSender().getId().equals(user.getId())
                && user.getRole() != UserRole.ADMIN) {
            throw new ApiException("غير مصرح لك بحذف الرسالة");
        }

        message.setDeleted(true);

        chatMessageRepository.save(message);

        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getChatRoom().getId() + "/delete",
                Map.of(
                        "messageId", message.getId()
                )
        );
    }

}
