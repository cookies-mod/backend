package dev.morazzer.cookies.backend.ws;

import dev.morazzer.cookies.backend.BackendApplication;
import dev.morazzer.cookies.backend.services.BackendRedisService;
import dev.morazzer.cookies.backend.utils.redis.PlayerKey;
import dev.morazzer.cookies.entities.misc.BackendVersion;
import dev.morazzer.cookies.entities.websocket.Packet;
import dev.morazzer.cookies.entities.websocket.packets.DungeonJoinPacket;
import dev.morazzer.cookies.entities.websocket.packets.DungeonLeavePacket;
import dev.morazzer.cookies.entities.websocket.packets.DungeonMimicKilledPacket;
import dev.morazzer.cookies.entities.websocket.packets.DungeonSyncPlayerLocation;
import dev.morazzer.cookies.entities.websocket.packets.DungeonUpdateRoomIdPacket;
import dev.morazzer.cookies.entities.websocket.packets.DungeonUpdateRoomSecrets;
import dev.morazzer.cookies.entities.websocket.packets.HandshakePacket;
import dev.morazzer.cookies.entities.websocket.packets.TestServerPacket;
import dev.morazzer.cookies.entities.websocket.packets.WrongProtocolVersionPacket;
import dev.morazzer.cookies.entities.websocket.packets.c2s.PlayersUseModRequestPacket;
import dev.morazzer.cookies.entities.websocket.packets.s2c.PlayersUseModResponsePacket;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import org.springframework.web.socket.CloseStatus;

public class PlayerListener {

    private final PlayerConnection playerConnection;
    private boolean hadHandshake = false;

    public PlayerListener(PlayerConnection playerConnection) {
        this.playerConnection = playerConnection;
    }

    public void handle(Packet<?> somePacket) {
        if (!hadHandshake && !(somePacket instanceof HandshakePacket)) {
            disconnect();
            return;
        }
        switch (somePacket) {
            case HandshakePacket packet -> this.handleHandshake(packet);
            case TestServerPacket packet -> this.ensureDev(packet, this::handleServerTest);
            case DungeonJoinPacket packet -> this.handleDungeonJoin(packet);
            case DungeonLeavePacket packet -> this.handleDungeonLeave(packet);
            case PlayersUseModRequestPacket packet -> this.playersUseModRequest(packet);

            case DungeonMimicKilledPacket packet -> this.sendToOthersInDungeon(packet);
            case DungeonSyncPlayerLocation packet -> this.sendToOthersInDungeon(packet);
            case DungeonUpdateRoomIdPacket packet -> this.sendToOthersInDungeon(packet);
            case DungeonUpdateRoomSecrets packet -> this.sendToOthersInDungeon(packet);

            default -> this.repostUnknown(somePacket);
        }
    }

    private void playersUseModRequest(PlayersUseModRequestPacket packet) {
        final HashMap<UUID, Boolean> map = new HashMap<>();
        for (UUID uuid : packet.uuids) {
            map.put(uuid, BackendRedisService.INSTANCE.get(new PlayerKey(uuid)));
        }
        this.playerConnection.sendPacket(new PlayersUseModResponsePacket(map));
    }

    private void handleHandshake(HandshakePacket packet) {
        if (packet.packetVersion == BackendVersion.CURRENT_PACKET_VERSION) {
            this.hadHandshake = true;
            this.playerConnection.sendPacket(packet);
            return;
        }

        this.playerConnection.sendPacket(new WrongProtocolVersionPacket(BackendVersion.CURRENT_PACKET_VERSION,
            packet.packetVersion,
            BackendVersion.CURRENT_PACKET_VERSION > packet.packetVersion));
        this.gracefulDisconnect();
    }

    private void handleDungeonLeave(DungeonLeavePacket packet) {
        final Optional<String> dungeonSession = this.playerConnection.getDungeonSession();
        if (dungeonSession.isEmpty()) {
            return;
        }
        this.playerConnection.removeScope(dungeonSession.get());
    }

    private <T extends Packet<T>> void ensureDev(T somePacket, Consumer<T> consumer) {
        if (!BackendApplication.devEnvironment) {
            return;
        }
        consumer.accept(somePacket);
    }

    private void repostUnknown(Packet<?> somePacket) {
        System.err.println("Unknown packet: " + somePacket.getClass().getSimpleName());
    }

    private void sendToOthersInDungeon(Packet<?> packet) {
        final Optional<String> dungeonSession = this.playerConnection.getDungeonSession();
        if (dungeonSession.isEmpty()) {
            return;
        }
        try {
            SocketHandler.sendGlobalWithScope(packet, dungeonSession.get());
        } catch (IOException ignored) {
            this.disconnect();
        }
    }

    private void gracefulDisconnect() {
        try {
            this.playerConnection.connection().close(CloseStatus.NORMAL);
        } catch (IOException e) {
        }
    }

    private void disconnect() {
        try {
            this.playerConnection.connection().close(CloseStatus.SERVER_ERROR);
        } catch (IOException ex) {
        }
    }

    private void handleDungeonJoin(DungeonJoinPacket dungeonJoinPacket) {
        final Optional<String> dungeonSession = this.playerConnection.getDungeonSession();
        dungeonSession.ifPresent(this.playerConnection.scopes()::remove);
        this.playerConnection.setDungeonSession("%s.%s".formatted(dungeonJoinPacket.getServer(),
            dungeonJoinPacket.getPartyLeader()));
    }

    private void handleServerTest(TestServerPacket testServerPacket) {
        System.out.println(
            testServerPacket.text + " " + this.playerConnection.uuid() + " " + this.playerConnection.name());
    }

}
