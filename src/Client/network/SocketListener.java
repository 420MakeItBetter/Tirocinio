package Client.network;


import Client.Main;
import Client.messages.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Matteo on 11/10/2016.
 */
public class SocketListener implements Runnable {

    Selector selector;
    Executor ex;


    public SocketListener(){
        try
        {
            selector = Selector.open();
            ServerSocketChannel skt = ServerSocketChannel.open();
            skt.configureBlocking(false);
            skt.bind(new InetSocketAddress(InetAddress.getLocalHost(),8333));
            skt.register(selector,SelectionKey.OP_ACCEPT);
            ex = Executors.newCachedThreadPool();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public void addSocket(SocketChannel skt,Object o) throws ClosedChannelException {
        skt.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, o);
        selector.wakeup();
    }


    @Override
    public void run() {

        while (true)
        {
            try
            {
                selector.selectedKeys().clear();
                selector.select();
                for(SelectionKey key : selector.selectedKeys())
                {
                    if(key.isAcceptable())
                    {
                        AcceptTask task = new AcceptTask(key,selector);
                        ex.execute(task);

                    }
                    else if(key.isReadable())
                    {
                        ReadTask task = new ReadTask(key,selector);
                        ex.execute(task);
                    }
                    else if(key.isWritable())
                    {
                        WriteTask task = new WriteTask(key,selector);
                        ex.execute(task);
                    }
                }

            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }
}
