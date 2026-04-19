package ch.uzh.ifi.hase.soprafs26.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;


@Component
public class WebSocketEventListener {

    private final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    //method to understand WS-disconnect better (for debugging reasons)
    //method is fired whenever a session disconnect event fires (can be both from client or server)
    //a session disconnect can happen when browser tab is closed, internet connection is lost, client deliberately asks for disconnect, or server crashes/timeouts
    //atm method only used for debugging reasons; in future it could be used to fire a msg to players if one player rage-quits or similar
    //to test if it works: ws-establish connection and close browser tab => debug msg in console
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event){

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        if (headerAccessor.getUser() != null) {
            String username = headerAccessor.getUser().getName();
            log.info("User disconnected: " + username);
            //here we could fire e.g., a "hey room XY, player2 left!" but maybe overkill
        }
    }
}
