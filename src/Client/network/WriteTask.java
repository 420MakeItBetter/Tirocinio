package Client.network;

import Client.BitConstants;
import Client.messages.SerializedMessage;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by Matteo on 11/10/2016.
 */
public class WriteTask implements Runnable {

    SelectionKey key;
    Selector selector;

    public WriteTask(SelectionKey key, Selector selector) {
        this.key = key;
        this.selector = selector;
    }

    @Override
    public void run() {

        SocketChannel channel = (SocketChannel) key.channel();
        Peer peer = (Peer) key.attachment();
        SerializedMessage msg = peer.getMsg();
        peer.setMsg(null);
        ByteBuffer magic = ByteBuffer.allocate(4);
        ByteBuffer command = ByteBuffer.allocate(12);
        ByteBuffer length = ByteBuffer.allocate(4);
        ByteBuffer checksum = ByteBuffer.allocate(4);
        magic.put(BitConstants.MAGIC);
        for(byte b : msg.getCommand().getBytes())
            command.put(b);

        int size = msg.getSize();
        length.put((byte) (size & 0xFF));
        length.put((byte) ((size >>> 8) & 0xFF));
        length.put((byte) ((size >>> 16) & 0xFF));
        length.put((byte) ((size >>> 24) & 0xFF));

        int check = msg.getChecksum();
        checksum.put((byte) (check & 0xFF));
        checksum.put((byte) ((check >>> 8) & 0xFF));
        checksum.put((byte) ((check >>> 16) & 0xFF));
        checksum.put((byte) ((check >>> 24) & 0xFF));

        try
        {
            channel.write(new ByteBuffer[]{magic,command,length,checksum,msg.getBuffer()});
            if(msg.getBuffer().position() < msg.getBuffer().capacity())
            {
                peer.setMsg(msg);
                channel.register(selector,SelectionKey.OP_WRITE,peer);
            }
            else
            {
                channel.register(selector,SelectionKey.OP_READ,peer);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
