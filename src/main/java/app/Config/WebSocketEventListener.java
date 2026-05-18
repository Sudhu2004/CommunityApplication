package app.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId();
        logger.info("New WebSocket connection established. Session ID: {}", sessionId);

        // You can extract user information from headers if needed
        // String userCode = headerAccessor.getFirstNativeHeader("userCode");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId();
        String userCode = (String) headerAccessor.getSessionAttributes().get("userCode");

        logger.info("WebSocket connection closed. Session ID: {}, User Code: {}",
                sessionId, userCode);

        // Notify other users that this user has disconnected
        if (userCode != null) {
            UserDisconnectedNotification notification = new UserDisconnectedNotification(userCode);
            messagingTemplate.convertAndSend("/topic/user-status", notification);
        }
    }

    public static class UserDisconnectedNotification {
        private String userCode;
        private long timestamp;

        public UserDisconnectedNotification(String userCode) {
            this.userCode = userCode;
            this.timestamp = System.currentTimeMillis();
        }

        public String getUserCode() {
            return userCode;
        }

        public void setUserCode(String userCode) {
            this.userCode = userCode;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
