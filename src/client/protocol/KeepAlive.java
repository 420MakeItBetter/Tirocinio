package client.protocol;

import client.messages.Ping;
import client.messages.Pong;
import client.network.Peer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.Random;

/**
 * Created by Matteo on 19/10/2016.
 *
 */
public class KeepAlive {

    private static Random rand = new Random();

    public static void sendPing(SocketChannel skt, Peer p) throws InterruptedException, ClosedChannelException {
        Ping  message = new Ping();
        message.setNonce(rand.nextLong());
        ByteBuffer header = ProtocolUtil.writeHeader(message);
        ByteBuffer payload = null;

        try
        {
            payload = ProtocolUtil.writePayload(message);
            header.put(ProtocolUtil.getChecksum(payload));
            ProtocolUtil.sendMessage(header,payload,skt,p);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    public static void sendPong(Ping msg,SocketChannel skt, Peer p) throws InterruptedException, ClosedChannelException {
        Pong resp = new Pong();
        resp.setNonce(msg.getNonce());
        ByteBuffer header = ProtocolUtil.writeHeader(resp);
        ByteBuffer payload = null;
        try
        {
            payload = ProtocolUtil.writePayload(resp);
            header.put(ProtocolUtil.getChecksum(payload));
            ProtocolUtil.sendMessage(header,payload,skt,p);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }

}
