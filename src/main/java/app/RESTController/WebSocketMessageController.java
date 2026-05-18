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

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    /**
     * Send a message to a community (Notices)
     * Client sends to: /app/community/{communityCode}/send
     */
    @MessageMapping("/community/{communityCode}/send")
    public void sendCommunityMessage(
            @DestinationVariable String communityCode,
            @Payload CreateMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor) {
        processMessage(null, communityCode, request, headerAccessor);
    }

    /**
     * Send a message to an event
     * Client sends to: /app/event/{eventCode}/send
     */
    @MessageMapping("/event/{eventCode}/send")
    public void sendMessage(
            @DestinationVariable String eventCode,
            @Payload CreateMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor) {
        processMessage(eventCode, null, request, headerAccessor);
    }

    private void processMessage(String eventCode, String communityCode,
                                CreateMessageRequest request, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String userCode = (String) headerAccessor.getSessionAttributes().get("userCode");
            if (userCode == null) throw new RuntimeException("User not authenticated");

            request.setEventCode(eventCode);
            request.setCommunityCode(communityCode);
            request.setGroupCode(null); // Explicitly null for consistency

            messageService.createMessage(request, userCode);
        } catch (Exception e) {
            if (headerAccessor.getUser() != null) {
                messagingTemplate.convertAndSendToUser(
                        headerAccessor.getUser().getName(),
                        "/queue/errors",
                        "Error sending message: " + e.getMessage()
                );
            }
        }
    }

    /**
     * Delete a message
     */
    @MessageMapping("/message/{messageId}/delete")
    public void deleteMessage(
            @DestinationVariable UUID messageId,
            SimpMessageHeaderAccessor headerAccessor) {

        try {
            String userCode = (String) headerAccessor.getSessionAttributes().get("userCode");
            if (userCode == null) throw new RuntimeException("User not authenticated");

            MessageDTO message = messageService.getMessageById(messageId);
            messageService.deleteMessage(messageId, userCode);

            String topic = null;
            if (message.getEventCode() != null) topic = "/topic/event/" + message.getEventCode() + "/messages";
            else if (message.getCommunityCode() != null) topic = "/topic/community/" + message.getCommunityCode() + "/messages";

            if (topic != null) {
                messagingTemplate.convertAndSend(topic + "/deleted", new MessageDeletedNotification(messageId, userCode));
            }

        } catch (Exception e) {
            if (headerAccessor.getUser() != null) {
                messagingTemplate.convertAndSendToUser(
                        headerAccessor.getUser().getName(),
                        "/queue/errors",
                        "Error deleting message: " + e.getMessage()
                );
            }
        }
    }

    // DTO for typing notifications
    public static class TypingNotification {
        private String userCode;
        private String userName;
        private boolean isTyping;

        public TypingNotification() {
        }

        public TypingNotification(String userCode, String userName, boolean isTyping) {
            this.userCode = userCode;
            this.userName = userName;
            this.isTyping = isTyping;
        }

        public String getUserCode() {
            return userCode;
        }

        public void setUserCode(String userCode) {
            this.userCode = userCode;
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
        private String userCode;

        public MessageDeletedNotification() {
        }

        public MessageDeletedNotification(UUID messageId, String userCode) {
            this.messageId = messageId;
            this.userCode = userCode;
        }

        public UUID getMessageId() {
            return messageId;
        }

        public void setMessageId(UUID messageId) {
            this.messageId = messageId;
        }

        public String getUserCode() {
            return userCode;
        }

        public void setUserCode(String userCode) {
            this.userCode = userCode;
        }
    }
}
