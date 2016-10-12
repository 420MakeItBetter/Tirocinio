package Client.network;

import Client.Main;
import Client.messages.SerializedMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by Matteo on 11/10/2016.
 */
public class ReadTask implements Runnable{

    private SelectionKey key;
    private Selector selector;

    public ReadTask(SelectionKey key,Selector selector) {
        this.key = key;
        this.selector = selector;
    }

    @Override
    public void run() {
        SocketChannel skt = (SocketChannel) key.channel();
        Peer peer = (Peer) key.attachment();
        SerializedMessage msg = null;
        if(peer.getMsg() == null)
        {
            msg = new SerializedMessage();
            ByteBuffer magic = ByteBuffer.allocate(4);
            ByteBuffer command = ByteBuffer.allocate(12);
            ByteBuffer length = ByteBuffer.allocate(4);
            ByteBuffer checksum = ByteBuffer.allocate(4);
            try
            {
                skt.read(new ByteBuffer[] {magic,command,length,checksum});
                msg.setCommand(new String(command.array()).trim());
                byte [] b = length.array();
                int size = ((b[3] & 0xFF) << 24 | (b[2] & 0xFF) << 16 | (b[1] & 0xFF) << 8 | (b[0] & 0xFF));
                b = checksum.array();
                int check = ((b[3] & 0xFF) << 24 | (b[2] & 0xFF) << 16 | (b[1] & 0xFF) << 8 | (b[0] & 0xFF));
                msg.setChecksum(check);
                ByteBuffer buffer = ByteBuffer.allocate(size);
                msg.setSize(size);
                msg.setBuffer(buffer);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            msg = peer.getMsg();
            peer.setMsg(null);
        }
        try
        {
            skt.read(msg.getBuffer());
            if(msg.getBuffer().position() < msg.getBuffer().capacity())
            {
                peer.setMsg(msg);
                skt.register(selector,SelectionKey.OP_READ,peer);
            }
            else
            {
                msg.getBuffer().rewind();
                peer.setMsg(msg);
                ComputeTask task = new ComputeTask(key,selector);
                Main.listener.ex.execute(task);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
