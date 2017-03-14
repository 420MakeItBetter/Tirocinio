package Client;

import Client.api.PublicInterface;
import Client.messages.PeerAddress;
import Client.messages.SerializedMessage;
import Client.messages.Version;
import Client.Protocol.Connect;
import Client.network.*;


import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Matteo on 07/10/2016.
 *
 */
public class Main {

    public static Socket client = null;

    public static Peer followed = null;


    public static PriorityBlockingQueue<Peer> newnotConnectedAdressess = new PriorityBlockingQueue<>();

    public static PriorityBlockingQueue<Peer> oldnotConnectedAdressess = new PriorityBlockingQueue<>();

    public static PriorityBlockingQueue<Peer> oldalreadyConnectedAdressess = new PriorityBlockingQueue<>();

    public static ConcurrentHashMap<String, Peer> peers = new ConcurrentHashMap<>();

    public static InventoryStat invStat = new InventoryStat();

    public static AtomicLong openedFiles = new AtomicLong(0);

    public static SocketListener listener = new SocketListener();

    public static PublicInterface publicInterface = new PublicInterface();

    public static void main(String [] args) {

        for(String s : BitConstants.DNS)
        {
            try
            {
                InetAddress [] adresses = InetAddress.getAllByName(s);
                for(InetAddress addr : adresses)
                {
                    if(!peers.containsKey(addr.getHostAddress()))
                    {
                        Peer p = new Peer(addr,BitConstants.PORT);
                        p.setTimestamp((int)(System.currentTimeMillis() / 1000));
                        peers.put(addr.getHostAddress(),p);
                        newnotConnectedAdressess.add(p);
                    }
                }
            } catch (UnknownHostException e)
            {
                e.printStackTrace();
            }
        }

        Thread thread = new Thread(listener);
        thread.start();
        new Thread(publicInterface).start();
        Thread externalListener = new Thread(new ExternalListener());
        externalListener.start();
        Thread keepAlive = new Thread(new KeepAliveTask());
        keepAlive.start();

        int counter = 0;
        while(true)
        {
            counter++;
            if(counter == 1250)
            {
             /*
                for(String s : BitConstants.DNS)
                {
                    try
                    {
                        InetAddress [] adresses = InetAddress.getAllByName(s);
                        for(InetAddress addr : adresses)
                        {
                            if(!peers.containsKey(addr.getHostAddress()))
                            {
                                Peer p = new Peer(addr,BitConstants.PORT);
                                p.setTimestamp((int)(System.currentTimeMillis() / 1000));
                                peers.put(addr.getHostAddress(),p);
                                newnotConnectedAdressess.add(p);
                            }
                        }
                    } catch (UnknownHostException e)
                    {
                        e.printStackTrace();
                    }
                }
                */
                counter = 0;
                try
                {
                    System.out.println("dormo");
                    Thread.currentThread().sleep(1000 * 60);
                    System.out.println("mi sveglio");
                } catch (InterruptedException e)
                {
                    break;
                }
            }
            if(counter < 750)
            {
                Peer p = newnotConnectedAdressess.poll();
                if(p == null)
                    p = oldalreadyConnectedAdressess.poll();
                if(p == null)
                    p = oldnotConnectedAdressess.poll();
                if(p != null)
                {
                    Connect.connect(p.getAddress(), p.getPort(), p);
                }

            }
            else
            {
                if(counter < 1000)
                {
                    Peer p = oldalreadyConnectedAdressess.poll();
                    if(p == null)
                        p = oldnotConnectedAdressess.poll();
                    if (p != null)
                    {
                        Connect.connect(p.getAddress(), p.getPort(), p);
                    }
                }
                else
                {
                    Peer p = oldnotConnectedAdressess.poll();
                    if(p != null)
                    {
                        Connect.connect(p.getAddress(),p.getPort(),p);
                    }
                }
            }
        }

    }

}
