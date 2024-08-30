package dev.morazzer.cookies.backend.ws;

import dev.morazzer.cookies.backend.BackendApplication;
import dev.morazzer.cookies.entities.websocket.Packet;
import dev.morazzer.cookies.entities.websocket.c2s.DungeonJoinPacket;
import dev.morazzer.cookies.entities.websocket.c2s.DungeonLeavePacket;
import dev.morazzer.cookies.entities.websocket.c2s.DungeonMimicKilledPacket;
import dev.morazzer.cookies.entities.websocket.c2s.DungeonSyncPlayerLocation;
import dev.morazzer.cookies.entities.websocket.c2s.DungeonUpdateRoomIdPacket;
import dev.morazzer.cookies.entities.websocket.c2s.DungeonUpdateRoomSecrets;
import dev.morazzer.cookies.entities.websocket.c2s.TestServerPacket;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;
import org.springframework.web.socket.CloseStatus;

public class PlayerListener {

    private final PlayerConnection playerConnection;

    public PlayerListener(PlayerConnection playerConnection) {
        this.playerConnection = playerConnection;
    }

    public void handle(Packet<?> somePacket) {
        switch (somePacket) {
            case TestServerPacket packet -> this.ensureDev(packet, this::handleServerTest);
            case DungeonJoinPacket packet -> this.handleDungeonJoin(packet);
            case DungeonLeavePacket packet -> this.handleDungeonLeave(packet);

            case DungeonMimicKilledPacket packet -> this.sendToOthersInDungeon(packet);
            case DungeonSyncPlayerLocation packet -> this.sendToOthersInDungeon(packet);
            case DungeonUpdateRoomIdPacket packet -> this.sendToOthersInDungeon(packet);
            case DungeonUpdateRoomSecrets packet -> this.sendToOthersInDungeon(packet);

            default -> this.repostUnknown(somePacket);
        }
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

    private void disconnect() {
        try {
            this.playerConnection.connection().close(CloseStatus.SERVER_ERROR);
        } catch (IOException ex) {
        }
    }

    private void handleDungeonJoin(DungeonJoinPacket dungeonJoinPacket) {
        final Optional<String> dungeonSession = this.playerConnection.getDungeonSession();
        dungeonSession.ifPresent(this.playerConnection.scopes()::remove);
        this.playerConnection.setDungeonSession("%s.%s".formatted(
            dungeonJoinPacket.getServer(),
            dungeonJoinPacket.getPartyLeader()));
    }

    private void handleServerTest(TestServerPacket testServerPacket) {
        System.out.println(testServerPacket.text + " " + this.playerConnection.uuid() + " " + this.playerConnection.name());
    }

}
