package app.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.UUID;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    private SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId();
        logger.info("New WebSocket connection established. Session ID: {}", sessionId);

        // You can extract user information from headers if needed
        // String userId = headerAccessor.getFirstNativeHeader("X-User-Id");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId();
        UUID userId = (UUID) headerAccessor.getSessionAttributes().get("userId");

        logger.info("WebSocket connection closed. Session ID: {}, User ID: {}",
                sessionId, userId);

        // Notify other users that this user has disconnected
        if (userId != null) {
            UserDisconnectedNotification notification = new UserDisconnectedNotification(userId);
            messagingTemplate.convertAndSend("/topic/user-status", notification);
        }
    }

    public static class UserDisconnectedNotification {
        private UUID userId;
        private long timestamp;

        public UserDisconnectedNotification(UUID userId) {
            this.userId = userId;
            this.timestamp = System.currentTimeMillis();
        }

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
