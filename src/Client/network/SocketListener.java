package Client.network;


import Client.BitConstants;
import Client.Main;
import Client.Protocol.Connect;
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


    Selector selector;
    ConcurrentLinkedQueue<SelectorParam> queue;
    Executor ex;
    ConcurrentLinkedQueue<Runnable> tasks;
    Logger logger;


    public SocketListener(){
        logger = LoggerFactory.getLogger(SocketListener.class);
        acceptNumber = new AtomicInteger();
        addressGetter = new AtomicInteger();
        computeNumber = new AtomicInteger();
        readNumber = new AtomicInteger();
        versionNumber = new AtomicInteger();
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
                    if(key.isConnectable())
                    {
                        connect(key);
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

    private void connect(SelectionKey key) {
        SocketChannel skt = (SocketChannel) key.channel();
        try
        {
            if(skt.finishConnect())
            {
                Connect.connections.incrementAndGet();
                VersionTask task = new VersionTask(skt, (Peer) key.attachment());
                ex.execute(task);
                addChannel(skt, 0, null);
            }
        } catch (IOException e)
        {
            try
            {
                Peer p = (Peer) key.attachment();
                p.incrementAttempt();
                Main.oldnotConnectedAdressess.add(p);
                skt.close();
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }
    }

    private void write(SelectionKey key) {
        SocketChannel skt = (SocketChannel) key.channel();
        Peer p = (Peer) key.attachment();
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
                skt.write(msg.getPayload());
                if (msg.getPayload()[msg.getPayload().length - 1].position() == msg.getPayload()[msg.getPayload().length - 1].limit())
                {
                    p.poolMsg();
                    SerializedMessage.returnHeader(msg.getHeader());
                    SerializedMessage.returnPayload(msg.getPayload());
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
                    SerializedMessage.returnHeader(msg.getHeader());
                }
                if (p.hasNoPendingMessage())
                    addChannel(skt, key.interestOps() & ~SelectionKey.OP_WRITE, p);
            }
        } catch (IOException e)
        {
            while(!p.hasNoPendingMessage())
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
                    }
                    try
                    {
                        SerializedMessage.returnPayload(m.getPayload());
                    } catch (InterruptedException e1)
                    {
                    }
            }
            p.setPeerState(PeerState.CLOSE);
            p.incrementAttempt();
            Main.oldnotConnectedAdressess.add(p);
            //e.printStackTrace();
        } catch (InterruptedException e)
        {}
    }

    private void read(SelectionKey key) {
        SocketChannel skt = (SocketChannel) key.channel();
        Peer p = (Peer) key.attachment();
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
            try
            {
                long read = skt.read(header);
                //se il canale e' stato chiuso allora la connessione e' stata chiusa
                if(read == -1)
                {

                    if(Main.showLog)
                        logger.error("il Peer {} ha chiuso la connessione", skt.getRemoteAddress());
                    //chiudo il socket
                    skt.close();
                    //setto lo stato del peer
                    p.setPeerState(PeerState.CLOSE);
                    //ritorno il buffer preso
                    p.incrementAttempt();
                    Main.oldnotConnectedAdressess.add(p);
                    SerializedMessage.returnHeader(header);

                    return;
                }
                header.position(16);
                byte [] b = new byte [4];
                header.get(b);
                int size = ((b[3] & 0xFF) << 24 | (b[2] & 0xFF) << 16 | (b[1] & 0xFF) << 8 | (b[0] & 0xFF));
                msg.setSize(size);
                //se il size del messaggio e' maggiore di 0 devo creare un payload per il messaggio
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
            } catch (IOException e)
            {
               // e.printStackTrace();
                try
                {
                    SerializedMessage.returnHeader(header);
                } catch (InterruptedException e1)
                {}
                p.incrementAttempt();
                p.setPeerState(PeerState.CLOSE);
                Main.oldnotConnectedAdressess.add(p);
                return;
            } catch (InterruptedException e)
            {}
        }
        else
        {
            msg = p.getMsg();
            p.setMsg(null);
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
                skt.read(msg.getPayload());
                if(msg.getPayload()[msg.getPayload().length - 1].position() < msg.getPayload()[msg.getPayload().length - 1].limit())
                {
                    p.setMsg(msg);
                    return;
                }

                ReadTask task = new ReadTask(skt,p,msg);
                ex.execute(task);
            } catch (IOException e)
            {
                try
                {
                    skt.close();
                } catch (IOException e1)
                {
                    e1.printStackTrace();
                }
                p.incrementAttempt();
                p.setPeerState(PeerState.CLOSE);
                try
                {
                    SerializedMessage.returnHeader(msg.getHeader());
                    SerializedMessage.returnPayload(msg.getPayload());
                } catch (InterruptedException e1)
                {}
                Main.oldnotConnectedAdressess.add(p);
                //e.printStackTrace();
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
