package client.protocol;

import client.BitConstants;
import client.Main;
import client.bitio.LittleEndianOutputStream;
import client.messages.Message;
import client.messages.SerializedMessage;
import client.network.Peer;
import client.utils.IOUtils;
import io.nayuki.bitcoin.crypto.Sha256;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by Matteo on 20/10/2016.
 *
 */
public class ProtocolUtil {

    public static ByteBuffer writePayload(Message m) throws InterruptedException, IOException {
        ByteBuffer payload = ByteBuffer.allocate((int) m.getLength());
        try(LittleEndianOutputStream out = LittleEndianOutputStream.wrap(payload))
        {
            m.write(out);
            return payload;
        }
    }

    public static ByteBuffer writeHeader(Message m) {
        ByteBuffer header = ByteBuffer.allocate(BitConstants.HEADERLENGTH);
        header.put(BitConstants.MAGIC);
        header.put(m.getCommand().getBytes());
        header.position(BitConstants.LENGTHPOSITION);
        header.put(IOUtils.intToByteArray(m.getLength()));
        return header;
    }

    public static byte [] getChecksum(ByteBuffer payload){
        payload.clear();
        return IOUtils.getChecksum(Sha256.getDoubleHash(payload.array()).toBytes());
    }

    public static void sendMessage(ByteBuffer header, ByteBuffer payload, SocketChannel skt, Peer p) throws ClosedChannelException {
        sendMessage(header,payload,skt,p,-1);
    }

    public static void sendMessage(ByteBuffer header, ByteBuffer payload, SocketChannel skt, Peer p, long id) throws ClosedChannelException {
        header.clear();
        if(payload != null)
            payload.clear();

        SerializedMessage message = new SerializedMessage();

        message.setCommand(new String(header.array(),BitConstants.COMMANDPOSITION,12).trim());
        message.setHeader(header);
        message.setPayload(payload);

        p.addMsg(message);
        message.setId(id);
        Main.listener.addChannel(skt,SelectionKey.OP_WRITE | SelectionKey.OP_READ,p);
    }
}
