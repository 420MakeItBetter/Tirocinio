package Client.network;


import Client.BitConstants;
import Client.Main;
import Client.messages.SerializedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Matteo on 11/10/2016.
 */
public class SocketListener implements Runnable {

    Selector selector;
    ConcurrentLinkedQueue<SelectorParam> queue;
    Executor ex;
    ConcurrentLinkedQueue<Runnable> tasks;
    Logger logger;


    public SocketListener(){
        logger = LoggerFactory.getLogger(SocketListener.class);
        try
        {
            selector = Selector.open();
            queue = new ConcurrentLinkedQueue<>();
            ServerSocketChannel skt = ServerSocketChannel.open();
            skt.configureBlocking(false);
            skt.bind(new InetSocketAddress(InetAddress.getLocalHost(),8333));
            skt.register(selector,SelectionKey.OP_ACCEPT);
            ex = Executors.newCachedThreadPool();
            tasks = new ConcurrentLinkedQueue<>();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public void addChannel(AbstractSelectableChannel channel, int interest, Object o) throws ClosedChannelException {
        queue.add(new SelectorParam(channel, interest, o));
        selector.wakeup();
    }


    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted())
        {
            try
            {
                selector.selectedKeys().clear();
                selector.select();
                for(SelectionKey key : selector.selectedKeys())
                {
                    if(key.isAcceptable())
                    {
                        accept(key);
                    }
                    else if(key.isReadable())
                    {
                        read(key);
                    }
                    else if(key.isWritable())
                    {
                        write(key);
                    }
                }
                while(!queue.isEmpty())
                {
                    SelectorParam param = queue.poll();
                    param.register(selector);
                }
                int count = 0;
                while(!tasks.isEmpty() && count < 5)
                {
                    Runnable r = tasks.poll();
                    ex.execute(r);
                }

            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }


    }

    private void write(SelectionKey key) {
        SocketChannel skt = (SocketChannel) key.channel();
        Peer p = (Peer) key.attachment();
        SerializedMessage msg = p.peekMsg();
        try
        {
            if(msg.getPayload() != null)
            {
                if(Main.showLog)
                    logger.info("Scritto messaggio {} inviati {} byte", msg.getCommand(), skt.write(new ByteBuffer[]{msg.getHeader(), msg.getPayload()}));
                else
                    skt.write(new ByteBuffer[]{msg.getHeader(), msg.getPayload()});
                if (msg.getPayload().position() == msg.getPayload().capacity())
                    p.poolMsg();
                if (p.hasNoPendingMessage())
                    addChannel(skt, key.interestOps() & ~SelectionKey.OP_WRITE, p);
            }
            else
            {
                if(Main.showLog)
                    logger.info("Scritto messaggio {} inviati {} byte",msg.getCommand(), skt.write(msg.getHeader()));
                else
                    skt.write(msg.getHeader());
                if (msg.getHeader().position() == msg.getHeader().capacity())
                    p.poolMsg();
                if (p.hasNoPendingMessage())
                    addChannel(skt, key.interestOps() & ~SelectionKey.OP_WRITE, p);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void read(SelectionKey key) {
        SocketChannel skt = (SocketChannel) key.channel();
        Peer p = (Peer) key.attachment();
        SerializedMessage msg = null;
        if(p.getMsg() == null)
        {
            msg = new SerializedMessage();
            ByteBuffer header = ByteBuffer.allocate(4 + 12 + 4 + 4);
            msg.setHeader(header);
            try
            {
                long read = skt.read(header);
                if(read == -1)
                {
                    if(Main.showLog)
                        logger.error("il Peer {} ha chiuso la connessione", skt.getRemoteAddress());
                    skt.close();
                    p.setPeerState(PeerState.CLOSE);
                    return;
                }
                header.position(16);
                byte [] b = new byte [4];
                header.get(b);
                int size = ((b[3] & 0xFF) << 24 | (b[2] & 0xFF) << 16 | (b[1] & 0xFF) << 8 | (b[0] & 0xFF));
                msg.setSize(size);
                if(size > 0)
                {
                    ByteBuffer payload = ByteBuffer.allocate(size);
                    msg.setPayload(payload);
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            msg = p.getMsg();
            p.setMsg(null);
        }
        if(msg.getSize() > 0)
        {
            try
            {
                skt.read(msg.getPayload());
                if(msg.getPayload().position() < msg.getPayload().capacity())
                {
                    p.setMsg(msg);
                }
                else
                {
                    ReadTask task = new ReadTask(skt,p,msg);
                    ex.execute(task);
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            ReadTask task = new ReadTask(skt,p,msg);
            ex.execute(task);
        }
    }

    private void accept(SelectionKey key){
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel skt = null;
        try
        {
            skt = server.accept();
            AcceptTask task = new AcceptTask(skt);
            ex.execute(task);
        } catch (IOException e)
        {
            e.printStackTrace();
        }


    }



}
