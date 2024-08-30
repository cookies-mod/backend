package dev.morazzer.cookies.backend.ws;

import dev.morazzer.cookies.backend.auth.JwtUtils;
import dev.morazzer.cookies.backend.services.MessageConfig;
import dev.morazzer.cookies.entities.websocket.Packet;
import dev.morazzer.cookies.entities.websocket.PacketSerializer;
import dev.morazzer.cookies.entities.websocket.Side;
import dev.morazzer.cookies.entities.websocket.c2s.DungeonMimicKilledPacket;
import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

public class SocketHandler implements WebSocketHandler {
    private static Set<PlayerConnection> sessions = new HashSet<>();
    private final JwtUtils jwtUtils;
    private static RabbitTemplate rabbitTemplate;

    public SocketHandler(JwtUtils jwtUtils, RabbitTemplate rabbitTemplate) {
        this.jwtUtils = jwtUtils;
        SocketHandler.rabbitTemplate = rabbitTemplate;
    }

    public static void sendToAllWithScope(Packet<?> packet, String scope) {
        try {
            byte[] serialized = Side.PACKETS.serializeUnknown(packet);
            for (PlayerConnection session : sessions) {
                if (session.isInScope(scope)) {
                    session.sendPacket(serialized);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendToAll_DO_NOT_USE(Packet<?> packet) {
        try {
            byte[] serialized = Side.PACKETS.serializeUnknown(packet);
            for (PlayerConnection session : sessions) {
                session.sendPacket(serialized);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendGlobalWithScope(Packet<?> packet, String s)
        throws IOException {
        final byte[] serialize = Side.PACKETS.serializeUnknown(packet);
        final PacketSerializer packetSerializer = new PacketSerializer();
        packetSerializer.writeString(s);
        packetSerializer.writeByteArray(serialize);
        rabbitTemplate.send(MessageConfig.EXCHANGE_NAME, "", new Message(packetSerializer.toByteArray()));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        final List<String> strings = session.getHandshakeHeaders()
            .computeIfAbsent(HttpHeaders.AUTHORIZATION, s -> Collections.singletonList(""));
        final Claims claims = jwtUtils.parseJwtClaims(jwtUtils.resolveToken(strings.getFirst()));

        sessions.add(new PlayerConnection(UUID.fromString(claims.getSubject()),
            claims.get("name", String.class),
            claims.get("version", String.class),
            new LinkedList<>(),
            session));
    }


    @Override
    public void handleMessage(@NotNull WebSocketSession session, @NotNull WebSocketMessage<?> message) {
        if (message instanceof BinaryMessage binaryMessage) {
            this.onBinaryMessage(session, binaryMessage);
        }
    }

    private void onBinaryMessage(WebSocketSession session, BinaryMessage binaryMessage) {
        try {
            Packet<?> packet = Side.PACKETS.deserialize(binaryMessage.getPayload().array());

            final PlayerConnection playerConnection = getPlayerConnection(session);
            if (playerConnection == null) {
                session.close(CloseStatus.SESSION_NOT_RELIABLE);
                return;
            }
            playerConnection.getPlayerListener().handle(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private PlayerConnection getPlayerConnection(WebSocketSession session) {
        for (PlayerConnection playerConnection : sessions) {
            if (playerConnection.equals(session)) {
                return playerConnection;
            }
        }
        return null;
    }

    @Override
    public void handleTransportError(@NotNull WebSocketSession session, @NotNull Throwable exception) {

    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus closeStatus) {
        sessions.removeIf(player -> player.equals(session));
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
