package Client.network;

import com.sun.org.apache.bcel.internal.generic.Select;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by Matteo on 11/10/2016.
 */
public class AcceptTask implements Runnable{

    SelectionKey key;
    Selector selector;

    public AcceptTask(SelectionKey key, Selector selector) {
        this.key = key;
        this.selector = selector;
    }


    @Override
    public void run() {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel skt = null;
        try
        {
            skt = server.accept();
            skt.configureBlocking(false);
            Peer peer = new Peer(skt.socket().getInetAddress(),skt.socket().getPort());
            skt.register(selector,SelectionKey.OP_READ,peer);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
