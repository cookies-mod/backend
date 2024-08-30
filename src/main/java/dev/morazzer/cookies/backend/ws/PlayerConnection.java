package dev.morazzer.cookies.backend.ws;

import dev.morazzer.cookies.entities.websocket.Packet;
import dev.morazzer.cookies.entities.websocket.Side;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

public final class PlayerConnection {
    private final UUID uuid;
    private final String name;
    private final String version;
    private final List<String> scopes;
    private final WebSocketSession connection;
    @Getter
    private final PlayerListener playerListener;

    public PlayerConnection(UUID uuid, String name, String version, List<String> scopes, WebSocketSession connection) {
        this.uuid = uuid;
        this.name = name;
        this.version = version;
        this.scopes = scopes;
        this.connection = connection;
        this.playerListener = new PlayerListener(this);
    }

    public boolean isInScope(String scope) {
        return !scopes.isEmpty() && scopes.stream().anyMatch(scope::equalsIgnoreCase);
    }

    public boolean equals(WebSocketSession session) {
        return this.connection.equals(session);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PlayerConnection other) {
            return this.uuid.equals(other.uuid);
        }
        if (obj instanceof WebSocketSession other) {
            return this.connection.equals(other);
        }
        return false;
    }

    public UUID uuid() {return uuid;}

    public String name() {return name;}

    public String version() {return version;}

    public List<String> scopes() {return scopes;}

    public WebSocketSession connection() {return connection;}

    public void sendPacket(Packet<?> packet) {
        try {
            sendPacket(Side.PACKETS.serializeUnknown(packet));
        } catch (IOException e) {
            try {
                connection.close(CloseStatus.SERVER_ERROR);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void sendPacket(byte[] bytes) {
        try {
            connection.sendMessage(new BinaryMessage(bytes));
        } catch (IOException e) {
            e.printStackTrace();
            try {
                connection.close(CloseStatus.SERVER_ERROR);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public Optional<String> getDungeonSession() {
        return scopes().stream().filter(s -> s.startsWith("dungeons.session")).findFirst();
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, scopes, connection);
    }

    @Override
    public String toString() {
        return "PlayerConnection[" + "uuid=" + uuid + ", " + "name=" + name + ", " + "scopes=" + scopes + ", " +
               "connection=" + connection + ']';
    }

    public void setDungeonSession(String formatted) {
        final String dungeonSession = "dungeons.session." + formatted;
        this.scopes.add(dungeonSession);
    }

    public void removeScope(String scope) {;
        this.scopes.remove(scope);
    }
}
