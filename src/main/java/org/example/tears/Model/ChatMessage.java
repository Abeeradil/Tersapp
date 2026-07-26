package org.example.tears.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.tears.Enums.MessageType;
import org.example.tears.Enums.ReadStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @Enumerated(EnumType.STRING)
    private ReadStatus readStatus;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String fileUrl;

    private String fileName;

    private Long fileSize;

    private Integer voiceDuration;

    private Boolean deleted = false;

    private Boolean mine;

    @CreationTimestamp
    private LocalDateTime createdAt;
}