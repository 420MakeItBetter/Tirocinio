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

    public static ByteBuffer[] writePayload(Message m) throws InterruptedException, IOException {
        ByteBuffer[] payload = SerializedMessage.newBlockingPayload((int) m.getLength());
        try
        {
            m.write(LittleEndianOutputStream.wrap(payload));
        } catch (IOException e)
        {
            SerializedMessage.returnPayload(payload);
            throw e;
        }
        return payload;
    }

    public static ByteBuffer writeHeader(Message m) throws InterruptedException {
        ByteBuffer header = SerializedMessage.newBlockingHeader();
        header.put(BitConstants.MAGIC);
        header.put(m.getCommand().getBytes());
        header.position(BitConstants.LENGTHPOSITION);
        header.put(IOUtils.intToByteArray(m.getLength()));
        return header;
    }

    public static byte [] getChecksum(ByteBuffer [] payload){

        for(ByteBuffer b : payload)
            b.flip();
        byte [] msg = new byte[(payload.length - 1)*500 + payload[payload.length - 1].limit()];
        int i;
        for(i = 0; i < payload.length - 1; i++)
            payload[i].get(msg,i*500,500);
        payload[payload.length - 1].get(msg,i*500,payload[payload.length - 1].limit());
            return IOUtils.getChecksum(Sha256.getDoubleHash(msg).toBytes());
    }

    public static void sendMessage(ByteBuffer header, ByteBuffer [] payload, SocketChannel skt, Peer p) throws ClosedChannelException {
        header.rewind();
        if(payload != null)
            for(int i = 0; i < payload.length; i++)
                payload[i].flip();

        SerializedMessage message = new SerializedMessage();

        message.setCommand(new String(header.array(),BitConstants.COMMANDPOSITION,12).trim());
        message.setHeader(header);
        message.setPayload(payload);

        p.addMsg(message);
        Main.listener.addChannel(skt,SelectionKey.OP_WRITE | SelectionKey.OP_READ,p);
    }

}
