package Client.network;


import Client.BitConstants;
import Client.Main;
import Client.Protocol.Connect;
import Client.messages.SerializedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
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
    public AtomicInteger connected;

    public AtomicInteger openedFiles;


    Selector selector;
    ConcurrentLinkedQueue<SelectorParam> queue;
    public Executor ex;
    ConcurrentLinkedQueue<Runnable> tasks;
    Logger logger;

    long wbytes;
    long rbytes;
    long connections;
    long dropped;
    long lastTime;
    FileOutputStream out;

    public SocketListener(){
        logger = LoggerFactory.getLogger(SocketListener.class);
        acceptNumber = new AtomicInteger();
        addressGetter = new AtomicInteger();
        computeNumber = new AtomicInteger();
        readNumber = new AtomicInteger();
        versionNumber = new AtomicInteger();
        connected = new AtomicInteger();

        openedFiles = new AtomicInteger();

        connections = 0;
        wbytes = 0;
        rbytes = 0;
        dropped = 0;
        File f = new File("nioStat");
        if(!f.exists())
            try
            {
                f.createNewFile();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        try
        {
            out = new FileOutputStream(f);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        try
        {
            selector = Selector.open();
            queue = new ConcurrentLinkedQueue<>();
            ServerSocketChannel skt = ServerSocketChannel.open();
            skt.configureBlocking(false);
            skt.bind(new InetSocketAddress(InetAddress.getByName("131.114.88.218"),8333));
            skt.register(selector,SelectionKey.OP_ACCEPT);
            ex = Executors.newCachedThreadPool();
            tasks = new ConcurrentLinkedQueue<>();
            System.out.println("Socket creato");
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

        lastTime = System.currentTimeMillis();
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
                        try
                        {
                            read(key);
                        }
                        catch(Exception e)
                        {
                            SocketChannel skt = (SocketChannel) key.channel();
                            dropped++;
                            Main.listener.openedFiles.decrementAndGet();
                            try
                            {
                                skt.close();
                            } catch (IOException e1)
                            {
                                e1.printStackTrace();
                            }
                            Peer p = (Peer) key.attachment();
                            p.incrementAttempt();
                            p.setVersion(false);
                            p.setPeerState(PeerState.CLOSE);
                            Main.oldnotConnectedAdressess.add(p);
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
                            dropped++;
                            Main.listener.openedFiles.decrementAndGet();
                            SocketChannel skt = (SocketChannel) key.channel();
                            try
                            {
                                skt.close();
                            } catch (IOException e1)
                            {
                                e1.printStackTrace();
                            }
                            Peer p = (Peer) key.attachment();
                            p.setPeerState(PeerState.CLOSE);
                            p.incrementAttempt();
                            p.setVersion(false);
                            Main.oldnotConnectedAdressess.add(p);
                            key.cancel();
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
                        while(p.hasNoPendingMessage())
                        {
                            SerializedMessage m = p.poolMsg();
                            try
                            {
                                SerializedMessage.returnHeader(m.getHeader());
                            } catch (InterruptedException e1)
                            {
                                e1.printStackTrace();
                            }
                            try
                            {
                                SerializedMessage.returnPayload(m.getPayload());
                            } catch (InterruptedException e1)
                            {
                                e1.printStackTrace();
                            }
                        }
                        SerializedMessage m = p.getMsg();
                        if(m != null)
                        {
                            try
                            {
                                SerializedMessage.returnHeader(m.getHeader());
                            } catch (InterruptedException e1)
                            {
                                e1.printStackTrace();
                            }
                            try
                            {
                                SerializedMessage.returnPayload(m.getPayload());
                            } catch (InterruptedException e1)
                            {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
                int count = 0;
                while(!tasks.isEmpty() && count < 5)
                {
                    Runnable r = tasks.poll();
                    ex.execute(r);
                }
                if(System.currentTimeMillis() - lastTime > 1000*60)
                {
                    ex.execute(new Stat(connections,dropped,wbytes,rbytes));
                    connections = 0;
                    dropped = 0;
                    wbytes = 0;
                    rbytes = 0;
                    lastTime = System.currentTimeMillis();
                }

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        for(SelectionKey k : selector.keys()){
            try
            {
                k.channel().close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
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
                wbytes+=skt.write(msg.getHeader());
                wbytes+=skt.write(msg.getPayload());
                if (msg.getPayload()[msg.getPayload().length - 1].position() == msg.getPayload()[msg.getPayload().length - 1].limit())
                {
                    p.poolMsg();
                    try
                    {
                        SerializedMessage.returnHeader(msg.getHeader());
                    } catch (InterruptedException e)
                    {}
                    try
                    {
                        SerializedMessage.returnPayload(msg.getPayload());
                    } catch (InterruptedException e)
                    {}
                }
                if (p.hasNoPendingMessage())
                    addChannel(skt, key.interestOps() & ~SelectionKey.OP_WRITE, p);
            }
            else
            {
                wbytes+=skt.write(msg.getHeader());
                if (msg.getHeader().position() == msg.getHeader().capacity())
                {
                    p.poolMsg();
                    try
                    {
                        SerializedMessage.returnHeader(msg.getHeader());
                    } catch (InterruptedException e)
                    {}
                }
                if (p.hasNoPendingMessage())
                    addChannel(skt, key.interestOps() & ~SelectionKey.OP_WRITE, p);
            }
        } catch (Exception e)
        {
            while(!p.hasNoPendingMessage())
            {
                SerializedMessage m = p.poolMsg();
                try
                {
                    SerializedMessage.returnHeader(m.getHeader());
                } catch (InterruptedException ignored)
                {}
                try
                {
                    SerializedMessage.returnPayload(m.getPayload());
                } catch (InterruptedException ignored)
                {}
            }
            SerializedMessage m = p.getMsg();
            if(m != null)
            {
                    try
                    {
                        SerializedMessage.returnHeader(m.getHeader());
                    } catch (InterruptedException ignored)
                    {}
                    try
                    {
                        SerializedMessage.returnPayload(m.getPayload());
                    } catch (InterruptedException ignored)
                    {}
            }
            throw e;
        }
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel skt = (SocketChannel) key.channel();
        Peer p = (Peer) key.attachment();
        p.setTime();
        SerializedMessage msg = null;
        //non ho iniziato a leggere il nuovo messaggio
        if(p.getMsg() == null)
        {
            //lo creo
            msg = new SerializedMessage();
            //prendo un header
            ByteBuffer header = SerializedMessage.newHeader();
            //se non ce ne sono disponibili ritorno quindi la prossima volta il peer di nuovo non avra' il messagio pronto
            if(header == null)
                return;
            //setto l'header
            msg.setHeader(header);
        }
        else
        {
            msg = p.getMsg();
            p.setMsg(null);
        }

        if(msg.getHeader().position() < msg.getHeader().limit())
        {
            try
            {
                int read = skt.read(msg.getHeader());
                if(read == -1)
                {
                    //chiudo il socket
                    skt.close();
                    //setto lo stato del peer
                    p.setPeerState(PeerState.CLOSE);
                    //ritorno il buffer preso
                    p.incrementAttempt();
                    Main.oldnotConnectedAdressess.add(p);
                    try
                    {
                        SerializedMessage.returnHeader(msg.getHeader());
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    return;
                }
                rbytes+=read;
                if(msg.getHeader().position() < msg.getHeader().limit())
                {
                    p.setMsg(msg);
                    return;
                }
                msg.getHeader().position(0);
                byte [] m = new byte [4];
                byte [] c = new byte [12];
                byte [] b = new byte [4];
                msg.getHeader().get(m);
                msg.getHeader().get(c);
                msg.getHeader().get(b);
                msg.getHeader().position(msg.getHeader().limit());
                int size = ((b[3] & 0xFF) << 24 | (b[2] & 0xFF) << 16 | (b[1] & 0xFF) << 8 | (b[0] & 0xFF));
                if(!Arrays.equals(m,BitConstants.MAGIC))
                    throw new IOException("Peer out of synch "+new String(c).trim());
                msg.setSize(size);
                msg.setCommand(new String(c).trim());
                //se il size del messaggio e' maggiore di 0 devo creare un payload per il messaggio
                if(size < 0)
                    System.err.println(size+" "+Integer.reverse(size)+" "+Integer.reverseBytes(size));
                if(size > 0)
                {
                    ByteBuffer [] payload = SerializedMessage.newPayload(size);
                    //se non e' disponibile un payload della dimensione richiesta
                    if(payload == null)
                    {
                        //salvo nel peer il messaggio con l'header e ritorno, la prossima volta che provo a leggere questo codice
                        //non sara' eseguito
                        p.setMsg(msg);
                        return;
                    }
                    msg.setPayload(payload);
                }
            } catch (Exception e)
            {
               // e.printStackTrace();

                try
                {
                    SerializedMessage.returnHeader(msg.getHeader());
                } catch (InterruptedException ignored)
                {}
                throw e;
            }

        }
        //se il size del messaggio e' maggiore di 0
        if(msg.getSize() > 0)
        {
            //controllo se il payload e' presente
            if(msg.getPayload() == null)
            {
                //altrimenti provo a prenderne uno
                msg.setPayload(SerializedMessage.newPayload(msg.getSize()));
                //se e' ancora null ritorno e riprovero' dopo
                if(msg.getPayload() == null)
                {
                    p.setMsg(msg);
                    return;
                }
            }
            try
            {
                rbytes+=skt.read(msg.getPayload());
                if(msg.getPayload()[msg.getPayload().length - 1].position() < msg.getPayload()[msg.getPayload().length - 1].limit())
                {
                    p.setMsg(msg);
                    return;
                }

                ReadTask task = new ReadTask(skt,p,msg);
                ex.execute(task);
            } catch (Exception e)
            {
                try
                {
                    SerializedMessage.returnHeader(msg.getHeader());
                }catch (InterruptedException ignored)
                {}
                try
                {
                    SerializedMessage.returnPayload(msg.getPayload());
                } catch (InterruptedException ignored)
                {}
                throw e;
            }
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
            connections++;
            System.out.println(Main.listener.openedFiles.incrementAndGet());
            AcceptTask task = new AcceptTask(skt);
            ex.execute(task);
        } catch (Exception e)
        {
            try
            {
                skt.close();
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }


    }


    private class Stat implements Runnable {

        long con;
        long drop;
        long wb;
        long rb;

        Stat(long c,long d,long w,long r){
            con = c;
            drop = d;
            wb = w;
            rb = r;
        }


        @Override
        public void run() {
            try
            {
                out.write(("Nuove Connessioni: "+con+"\n").getBytes());
                out.write(("Connessioni cadute: "+drop+"\n").getBytes());
                out.write(("Byte Scritti: "+wb+"\n").getBytes());
                out.write(("Byte Letti: "+rb+"\n\n").getBytes());
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
