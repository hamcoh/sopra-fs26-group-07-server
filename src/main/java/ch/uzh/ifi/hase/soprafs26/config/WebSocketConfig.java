package ch.uzh.ifi.hase.soprafs26.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker //enables WebSocket message handling backed by message broker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WsAuthChannelInterceptor wsAuthChannelInterceptor;

    @Override //method needed to validate user-token
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(wsAuthChannelInterceptor); //class that intercepts the token on CONNECT frame (after the handshake)
    }

    //method that configures message routing between the messaging parties
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) { //configures message broker
        config.enableSimpleBroker("/topic"); //'server -> client': enable in-memory message broker to carry messages back to client on destinations prefixed with “/topic” (MAYBE: add heartbeat config to monitor connections!)
        config.setApplicationDestinationPrefixes("/app"); //'client -> server': defines namespace for messages send by clients
    }

    //method for integrating STOMP (Simple/Streaming Text Oriented Message Protocol), i.e., simple text-based protocol (similar to HTTP)
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) { 
        registry.addEndpoint("/ws") //'ws' is the endpoint where clients establish connection (controller methods are associated to this endpoint)
                .setAllowedOrigins("*"); //CHANGE THIS SETTING when it works to prevent untrusted domains to make requests
                // .withSockJS(); //fallback when native WebSockets are not supported (then: emulates HTTP-behaviour)
                
    }
}
