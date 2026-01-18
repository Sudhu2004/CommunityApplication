package app.RESTController;

import app.DTO.Message.CreateMessageRequest;
import app.DTO.Message.MessageDTO;
import app.Service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class WebSocketMessageController {

    @Autowired
    private MessageService messageService;

    private SimpMessageSendingOperations messagingTemplate;

    /**
     * Send a message to an event
     * Client sends to: /app/event/{eventId}/send
     * Broadcasts to: /topic/event/{eventId}/messages
     */
    @MessageMapping("/event/{eventId}/send")
    public void sendMessage(
            @DestinationVariable UUID eventId,
            @Payload CreateMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor) {

        try {
            // Get user ID from session attributes
            UUID userId = (UUID) headerAccessor.getSessionAttributes().get("userId");

            if (userId == null) {
                throw new RuntimeException("User not authenticated");
            }

            // Set the event ID from the path
            request.setEventId(eventId);

            // Create the message
            MessageDTO savedMessage = messageService.createMessage(request, userId);

            // Broadcast to all subscribers of this event
            messagingTemplate.convertAndSend(
                    "/topic/event/" + eventId + "/messages",
                    savedMessage
            );

        } catch (Exception e) {
            // Send error to the specific user
            messagingTemplate.convertAndSendToUser(
                    headerAccessor.getUser().getName(),
                    "/queue/errors",
                    "Error sending message: " + e.getMessage()
            );
        }
    }

    /**
     * User typing indicator
     * Client sends to: /app/event/{eventId}/typing
     * Broadcasts to: /topic/event/{eventId}/typing
     */
    @MessageMapping("/event/{eventId}/typing")
    @SendTo("/topic/event/{eventId}/typing")
    public TypingNotification handleTyping(
            @DestinationVariable UUID eventId,
            @Payload TypingNotification notification) {
        return notification;
    }

    /**
     * Delete a message
     * Client sends to: /app/message/{messageId}/delete
     */
    @MessageMapping("/message/{messageId}/delete")
    public void deleteMessage(
            @DestinationVariable UUID messageId,
            SimpMessageHeaderAccessor headerAccessor) {

        try {
            UUID userId = (UUID) headerAccessor.getSessionAttributes().get("userId");

            if (userId == null) {
                throw new RuntimeException("User not authenticated");
            }

            // Get the message to find its event ID
            MessageDTO message = messageService.getMessageById(messageId);

            // Delete the message
            messageService.deleteMessage(messageId, userId);

            // Notify all subscribers
            messagingTemplate.convertAndSend(
                    "/topic/event/" + message.getEventId() + "/message-deleted",
                    new MessageDeletedNotification(messageId, userId)
            );

        } catch (Exception e) {
            messagingTemplate.convertAndSendToUser(
                    headerAccessor.getUser().getName(),
                    "/queue/errors",
                    "Error deleting message: " + e.getMessage()
            );
        }
    }

    // DTO for typing notifications
    public static class TypingNotification {
        private UUID userId;
        private String userName;
        private boolean isTyping;

        public TypingNotification() {
        }

        public TypingNotification(UUID userId, String userName, boolean isTyping) {
            this.userId = userId;
            this.userName = userName;
            this.isTyping = isTyping;
        }

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public boolean isTyping() {
            return isTyping;
        }

        public void setTyping(boolean typing) {
            isTyping = typing;
        }
    }

    // DTO for message deletion notifications
    public static class MessageDeletedNotification {
        private UUID messageId;
        private UUID deletedBy;

        public MessageDeletedNotification() {
        }

        public MessageDeletedNotification(UUID messageId, UUID deletedBy) {
            this.messageId = messageId;
            this.deletedBy = deletedBy;
        }

        public UUID getMessageId() {
            return messageId;
        }

        public void setMessageId(UUID messageId) {
            this.messageId = messageId;
        }

        public UUID getDeletedBy() {
            return deletedBy;
        }

        public void setDeletedBy(UUID deletedBy) {
            this.deletedBy = deletedBy;
        }
    }
}
