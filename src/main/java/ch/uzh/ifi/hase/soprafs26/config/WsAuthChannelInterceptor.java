package ch.uzh.ifi.hase.soprafs26.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import ch.uzh.ifi.hase.soprafs26.service.UserService;

@Component
public class WsAuthChannelInterceptor implements ChannelInterceptor { //intercepts messages flowing through channel

    @Autowired
    private UserService userService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) { //method 'preSend' is called before every msg is send through channel (however, only on 'CONNECT' sth happens)
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message); //encode message to STOMP frame, s.t., we can access STOMP-headers (=> token is sent as STOMP-header)

        if (StompCommand.CONNECT.equals(accessor.getCommand())) { //validates only at the beginning of the session
            String token = accessor.getFirstNativeHeader("token"); //get the token
            try { //work-around because of HTTP-erros thrown in "verifyToken" (might refactor!)
                userService.verifyToken(token); //delegate to userService
            }
            catch (Exception e){
                throw new MessagingException("Unauthorized");
            } 
        }
        return message;
    } 
}
