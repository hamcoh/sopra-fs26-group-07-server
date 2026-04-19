package ch.uzh.ifi.hase.soprafs26.websocket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import ch.uzh.ifi.hase.soprafs26.config.WsAuthChannelInterceptor;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

//tests whether token validation works after ws-handshake is established (crucial before actually establishing connection)
@ExtendWith(MockitoExtension.class)
public class WsAuthChannelInterceptorTest {

    @Mock
    private UserService userService;

    @Mock
    private MessageChannel channel;

    @InjectMocks
    private WsAuthChannelInterceptor interceptor;

    //CONNECT message after handshake should throw a MessagingException if user is unauthorized (=invalid token)
    @Test
    void failEstablishWsConnection_invalidToken_throwsMessagingException() {

        //build fake incoming WebSocket CONNECT Frame
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("token", "invalidToken");
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Mockito.doThrow(new MessagingException("Unauthorized")).when(userService).verifyToken("invalidToken");
        
        assertThrows(MessagingException.class, () -> {
            interceptor.preSend(message, channel);
        });
    }

    //CONNECT frame should pass if token is valid => no error thrown
    @Test
    void establishWsConnection_validToken_success() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("token", "validToken");
        accessor.setLeaveMutable(true);
        Message<?> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        doNothing().when(userService).verifyToken("validToken");
        Mockito.when(userService.getUserbyToken("validToken")).thenReturn(testUser);

        Message<?> result = interceptor.preSend(message, channel);

        StompHeaderAccessor resultAccessor = StompHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);
        assertNotNull(resultAccessor.getUser());
        assertEquals(testUser.getUsername(), resultAccessor.getUser().getName());
    }
}
