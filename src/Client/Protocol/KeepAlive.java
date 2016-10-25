package Client.Protocol;

import Client.messages.Ping;
import Client.messages.Pong;
import Client.network.Peer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Random;

/**
 * Created by Matteo on 19/10/2016.
 *
 */
public class KeepAlive {

    private static Random rand = new Random();

    public static void sendPing(SocketChannel skt, Peer p) throws IOException {
        Ping  message = new Ping();
        message.setNonce(rand.nextLong());
        ByteBuffer header = ProtocolUtil.writeHeader(message);
        ByteBuffer payload = ProtocolUtil.writePayload(message);
        header.put(ProtocolUtil.getChecksum(payload));

        ProtocolUtil.sendMessage(header,payload,skt,p);
    }

    public static void sendPong(Ping msg,SocketChannel skt, Peer p) throws IOException {
        Pong resp = new Pong();
        resp.setNonce(msg.getNonce());
        ByteBuffer header = ProtocolUtil.writeHeader(resp);
        ByteBuffer payload = ProtocolUtil.writePayload(resp);
        header.put(ProtocolUtil.getChecksum(payload));

        ProtocolUtil.sendMessage(header,payload,skt,p);
    }

}
