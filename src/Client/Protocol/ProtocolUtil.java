package Client.Protocol;

import Client.BitConstants;
import Client.Main;
import Client.bitio.LittleEndianOutputStream;
import Client.messages.Message;
import Client.messages.SerializedMessage;
import Client.network.Peer;
import Client.utils.IOUtils;
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

    public static ByteBuffer writePayload(Message m) throws IOException {
        ByteBuffer payload = ByteBuffer.allocate(m.getLength());
        m.write(LittleEndianOutputStream.wrap(payload));
        return payload;
    }

    public static ByteBuffer writeHeader(Message m){
        ByteBuffer header = ByteBuffer.allocate(BitConstants.HEADERLENGTH);
        header.put(BitConstants.MAGIC);
        header.put(m.getCommand().getBytes());
        header.position(BitConstants.LENGTHPOSITION);
        header.put(IOUtils.intToByteArray(m.getLength()));
        return header;
    }

    public static byte [] getChecksum(ByteBuffer payload){
        return IOUtils.getChecksum(Sha256.getDoubleHash(payload.array()).toBytes());
    }

    public static void sendMessage(ByteBuffer header, ByteBuffer payload, SocketChannel skt, Peer p) throws ClosedChannelException {
        header.rewind();
        if(payload != null)
            payload.rewind();

        SerializedMessage message = new SerializedMessage();

        message.setCommand(new String(header.array(),BitConstants.COMMANDPOSITION,12).trim());
        message.setHeader(header);
        message.setPayload(payload);

        p.addMsg(message);
        Main.listener.addChannel(skt,SelectionKey.OP_WRITE | SelectionKey.OP_READ,p);
    }

}
