package ch.uzh.ifi.hase.soprafs26.websocket;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.security.Principal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

class WebSocketEventListenerTest {

    private WebSocketEventListener webSocketEventListener;

    @BeforeEach
    void setUp() {
        webSocketEventListener = new WebSocketEventListener();
    }

    @Test
    void handleWebSocketDisconnectListener_authenticatedUser_doesNotThrow() {
        // Arrange
        SimpMessageHeaderAccessor accessor = StompHeaderAccessor.create();
        accessor.setUser(new TestPrincipal("testUser"));

        Message<byte[]> message = MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders()
        );

        SessionDisconnectEvent event = new SessionDisconnectEvent(
                this,
                message,
                "session-id",
                CloseStatus.NORMAL
        );

        // Act + Assert
        assertDoesNotThrow(() ->
                webSocketEventListener.handleWebSocketDisconnectListener(event)
        );
    }

    @Test
    void handleWebSocketDisconnectListener_missingUser_doesNotThrow() {
        // Arrange
        SimpMessageHeaderAccessor accessor = StompHeaderAccessor.create();

        Message<byte[]> message = MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders()
        );

        SessionDisconnectEvent event = new SessionDisconnectEvent(
                this,
                message,
                "session-id",
                CloseStatus.NORMAL
        );

        // Act + Assert
        assertDoesNotThrow(() ->
                webSocketEventListener.handleWebSocketDisconnectListener(event)
        );
    }

    private static class TestPrincipal implements Principal {
        private final String name;

        private TestPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}