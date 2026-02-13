package me.ifmo.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    public static final String WS_ENDPOINT = "/ws";
    public static final String TOPIC_PREFIX = "/topic";
    public static final String APP_DESTINATION_PREFIX = "/app";

    @Value("${app.websocket.allowed-origins:*}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(TOPIC_PREFIX);
        registry.setApplicationDestinationPrefixes(APP_DESTINATION_PREFIX);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(WS_ENDPOINT)
                .setAllowedOriginPatterns(allowedOrigins);

        registry.addEndpoint(WS_ENDPOINT)
                .setAllowedOriginPatterns(allowedOrigins)
                .withSockJS();
    }
}
