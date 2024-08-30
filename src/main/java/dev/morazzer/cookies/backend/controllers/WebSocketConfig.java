package dev.morazzer.cookies.backend.controllers;

import dev.morazzer.cookies.backend.auth.JwtUtils;
import dev.morazzer.cookies.backend.ws.SocketHandler;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final JwtUtils jwtUtils;
    private final RabbitTemplate rabbitTemplate;

    public WebSocketConfig(JwtUtils jwtUtils, RabbitTemplate rabbitTemplate) {
        this.jwtUtils = jwtUtils;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new SocketHandler(jwtUtils, rabbitTemplate), "/websocket")
            .setAllowedOrigins("*");
    }
}
