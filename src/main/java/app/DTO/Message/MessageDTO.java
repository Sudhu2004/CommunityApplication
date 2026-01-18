package app.DTO.Message;

import app.Database.MessageType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageDTO {
    private UUID id;
    private UUID eventId;
    private UUID senderId;
    private String senderName;
    private String senderProfilePhotoUrl;
    private MessageType type;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MediaDTO> mediaList = new ArrayList<>();

    public MessageDTO() {
    }

    public MessageDTO(UUID id, UUID eventId, UUID senderId, String senderName,
                      String senderProfilePhotoUrl, MessageType type, String content,
                      LocalDateTime createdAt, LocalDateTime updatedAt, List<MediaDTO> mediaList) {
        this.id = id;
        this.eventId = eventId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderProfilePhotoUrl = senderProfilePhotoUrl;
        this.type = type;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.mediaList = mediaList;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderProfilePhotoUrl() {
        return senderProfilePhotoUrl;
    }

    public void setSenderProfilePhotoUrl(String senderProfilePhotoUrl) {
        this.senderProfilePhotoUrl = senderProfilePhotoUrl;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<MediaDTO> getMediaList() {
        return mediaList;
    }

    public void setMediaList(List<MediaDTO> mediaList) {
        this.mediaList = mediaList;
    }
}
