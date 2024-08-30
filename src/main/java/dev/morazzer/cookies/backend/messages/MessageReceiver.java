package dev.morazzer.cookies.backend.messages;

import dev.morazzer.cookies.backend.ws.SocketHandler;
import dev.morazzer.cookies.entities.websocket.Packet;
import dev.morazzer.cookies.entities.websocket.PacketSerializer;
import dev.morazzer.cookies.entities.websocket.Side;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class MessageReceiver {

    public void receiveMessage(byte[] message) {
        try {
            PacketSerializer serializer = new PacketSerializer(message);
            final String scope = serializer.readString();
            Packet<?> packet = Side.PACKETS.deserialize(serializer.readByteArray());
            SocketHandler.sendToAllWithScope(packet, scope);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
