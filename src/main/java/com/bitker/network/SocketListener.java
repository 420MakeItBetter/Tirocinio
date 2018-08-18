package com.bitker.network;


import com.bitker.utils.BitConstants;
import com.bitker.Main;
import com.bitker.eventservice.EventService;
import com.bitker.eventservice.events.ConnectedEvent;
import com.bitker.eventservice.events.MessageSentEvent;
import com.bitkermessage.client.messages.messages.SerializedMessage;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Matteo on 11/10/2016.
 *
 */
public class SocketListener implements Runnable {

    public  AtomicInteger acceptNumber;
    public  AtomicInteger addressGetter;
    public  AtomicInteger computeNumber;
    public  AtomicInteger readNumber;
    public AtomicInteger versionNumber;
    public AtomicInteger connectNumber;
    public AtomicInteger connected;
    private ServerSocketChannel skt;

    public Selector selector;
    ConcurrentLinkedQueue<SelectorParam> queue;
    public Executor ex;


    public SocketListener(){
        acceptNumber = new AtomicInteger();
        addressGetter = new AtomicInteger();
        computeNumber = new AtomicInteger();
        readNumber = new AtomicInteger();
        versionNumber = new AtomicInteger();
        connectNumber = new AtomicInteger();
        connected = new AtomicInteger();


        try{
            selector = Selector.open();
            ex = Executors.newFixedThreadPool(3000);
            queue = new ConcurrentLinkedQueue<>();
            skt = ServerSocketChannel.open();
            Main.openedFiles.incrementAndGet();
            skt.setOption(StandardSocketOptions.SO_REUSEADDR,true);
            skt.configureBlocking(false);
            //skt.bind(new InetSocketAddress(InetAddress.getLocalHost(),8333));
            skt.bind(new InetSocketAddress(InetAddress.getByName("131.114.2.151"),8333));
            skt.register(selector,SelectionKey.OP_ACCEPT);
            System.out.println("Socket creato");
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public void addChannel(AbstractSelectableChannel channel, int interest, Peer o) throws ClosedChannelException {
        queue.add(new SelectorParam(channel, interest, o));
        selector.wakeup();
    }


    @Override
    public void run() {

        long time = System.currentTimeMillis();
        while (!Main.terminate.get())
        {
            if(System.currentTimeMillis() - time > 1000*60)
            {
                System.out.println("SocketListener Alive");
                time = System.currentTimeMillis();
            }
            try
            {
                selector.selectedKeys().clear();
                selector.select();
                for(SelectionKey key : selector.selectedKeys())
                {
                    if(!key.isValid())
                    {
                        key.cancel();
                    }
                    else if(key.isAcceptable())
                    {
                        accept(key);
                    }
                    else if(key.isConnectable())
                    {
                        try{
                            connect(key);
                        }catch (Exception e)
                        {
                            SocketChannel skt = (SocketChannel) key.channel();
                            try{
                                skt.close();
                            }catch (IOException e1)
                            {
                                e1.printStackTrace();
                            }
                            Peer p = (Peer) key.attachment();
                            p.close();
                            key.cancel();
                        }
                    }
                    else if(key.isReadable())
                    {
                        try
                        {
                            read(key);
                        }
                        catch(Exception e)
                        {
                            SocketChannel skt = (SocketChannel) key.channel();
                            try
                            {
                                skt.close();
                                Main.openedFiles.decrementAndGet();
                            } catch (IOException e1)
                            {
                                e1.printStackTrace();
                            }
                            Peer p = (Peer) key.attachment();
                            p.close();
                            key.cancel();
                        }
                    }
                    else if(key.isWritable())
                    {
                        try
                        {
                            write(key);
                        }catch (Exception e)
                        {
                            SocketChannel skt = (SocketChannel) key.channel();
                            try
                            {
                                skt.close();
                                Main.openedFiles.decrementAndGet();
                            }catch (IOException e1)
                            {
                                e1.printStackTrace();
                            }
                            Peer p = (Peer) key.attachment();
                            p.close();
                            key.cancel();
                            e.printStackTrace();
                        }
                    }
                }
                while(!queue.isEmpty())
                {
                    SelectorParam param = queue.poll();
                    try
                    {
                        param.register(selector);
                    }catch (ClosedChannelException e)
                    {
                        Peer p = param.getInterest();
                        p.close();
                    }
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        try
        {
            skt.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        ((ThreadPoolExecutor) ex).shutdownNow();
    }

    private void connect(SelectionKey key) throws IOException {
        SocketChannel skt = (SocketChannel) key.channel();
        Peer p = (Peer) key.attachment();
        if(skt.finishConnect())
        {
            p.setPeerState(PeerState.HANDSHAKE);
            p.setConnectionTime(System.currentTimeMillis());
            VersionTask t = new VersionTask(skt,p);
            ex.execute(t);
            addChannel(skt,SelectionKey.OP_READ,p);
            EventService.getInstance().publish(new ConnectedEvent(p));
        }
        else
        {
            if(System.currentTimeMillis() - p.getTime() > 10000)
                p.close();
        }
    }

    private void write(SelectionKey key) throws IOException {
        SocketChannel skt = (SocketChannel) key.channel();
        Peer p = (Peer) key.attachment();
        p.setTime();
        SerializedMessage msg = p.peekMsg();
        try
        {
            if(msg == null)
            {
                addChannel(skt, key.interestOps() & ~SelectionKey.OP_WRITE, p);
                return;
            }
            if(msg.getPayload() != null)
            {
                skt.write(msg.getHeader());
                if(msg.getHeader().position() != msg.getHeader().capacity())
                    return;
                skt.write(msg.getPayload());
                if (msg.getPayload().position() == msg.getPayload().capacity())
                {
                    p.poolMsg();
                }
                if (p.hasNoPendingMessage())
                    addChannel(skt, key.interestOps() & ~SelectionKey.OP_WRITE, p);

            }
            else
            {
                skt.write(msg.getHeader());
                if (msg.getHeader().position() == msg.getHeader().capacity())
                {
                    p.poolMsg();
                }
                if (p.hasNoPendingMessage())
                    addChannel(skt, key.interestOps() & ~SelectionKey.OP_WRITE, p);
            }
            EventService.getInstance().publish(new MessageSentEvent(p,msg));
        } catch (Exception e)
        {
            throw e;
        }
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel skt = (SocketChannel) key.channel();
        Peer p = (Peer) key.attachment();
        p.setTime();
        SerializedMessage msg = null;
        if(p.getMsg() == null)
        {
            msg = new SerializedMessage();
            ByteBuffer header = ByteBuffer.allocate(BitConstants.HEADERLENGTH);
            msg.setHeader(header);
        }
        else
        {
            msg = p.getMsg();
            p.setMsg(null);
        }

        if(msg.getHeader().position() < msg.getHeader().limit())
        {
            int read = skt.read(msg.getHeader());
            if (read == -1)
            {
                p.close();
                return;
            }
            if (msg.getHeader().position() < msg.getHeader().limit())
            {
                p.setMsg(msg);
                return;
            }
            msg.getHeader().position(0);
            byte[] m = new byte[4];
            byte[] c = new byte[12];
            byte[] b = new byte[4];
            msg.getHeader().get(m);
            msg.getHeader().get(c);
            msg.getHeader().get(b);
            msg.getHeader().position(msg.getHeader().limit());
            int size = ((b[3] & 0xFF) << 24 | (b[2] & 0xFF) << 16 | (b[1] & 0xFF) << 8 | (b[0] & 0xFF));
            if (!Arrays.equals(m, BitConstants.MAGIC))
                throw new IOException("Peer out of synch " + new String(c).trim());
            msg.setSize(size);
            msg.setCommand(new String(c).trim());

            if (size > 0)
            {
                ByteBuffer payload = ByteBuffer.allocate(size);
                msg.setPayload(payload);
            }
        }
        if(msg.getSize() > 0)
        {
            skt.read(msg.getPayload());
            if (msg.getPayload().position() < msg.getPayload().limit())
            {
                p.setMsg(msg);
                return;
            }
            ReadTask task = new ReadTask(skt, p, msg);
            ex.execute(task);
        }
        else
        {
            ReadTask task = new ReadTask(skt,p,msg);
            ex.execute(task);
        }
    }

    private void accept(SelectionKey key){
        connected.incrementAndGet();
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel skt = null;
        try
        {
            skt = server.accept();
            Main.openedFiles.incrementAndGet();
            AcceptTask task = new AcceptTask(skt);
            ex.execute(task);
        } catch (Exception e)
        {
            try
            {
                skt.close();
                Main.openedFiles.decrementAndGet();
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }


    }
}
