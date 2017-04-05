package client;

import client.api.PublicInterface;
import client.protocol.Connect;
import client.network.*;


import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Matteo on 07/10/2016.
 *
 */
public class Main {

    public static Socket client = null;

    public static Peer followed = null;

    public static ConcurrentHashMap<String, Peer> peers = new ConcurrentHashMap<>();

    public static InventoryStat invStat = new InventoryStat();

    public static AtomicLong openedFiles = new AtomicLong(0);

    public static SocketListener listener = new SocketListener();

    public static PublicInterface publicInterface = new PublicInterface();
    public static AtomicBoolean terminate = new AtomicBoolean(false);

    public static ReentrantLock connLock = new ReentrantLock();

    public static void main(String [] args) {

        File o = new File("out"+System.currentTimeMillis());
        if(!o.exists())
            try
            {
                o.createNewFile();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

        FileOutputStream out = null;
        try
        {
            out = new FileOutputStream(o);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        System.setOut(new PrintStream(out));

        File ee = new File("err"+System.currentTimeMillis());
        if(!ee.exists())
            try
            {
                ee.createNewFile();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

        FileOutputStream err = null;
        try
        {
            err = new FileOutputStream(ee);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        System.setErr(new PrintStream(err));
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
                    }
                }
            } catch (UnknownHostException e)
            {
                e.printStackTrace();
            }
        }

        Thread thread = new Thread(listener);
        thread.start();
        Thread pubThread = new Thread(publicInterface);
        pubThread.start();
        Thread externalListener = new Thread(new ExternalListener());
        externalListener.start();
        Thread keepAlive = new Thread(new KeepAliveTask());
        keepAlive.start();


        restoreConnection();
        int counter = 0;
        while(!terminate.get())
        {
            System.out.println("dormo");
            try
            {
                Thread.currentThread().sleep(1000*60);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            if(listener.connectNumber.get() > 0)
                continue;
            System.out.println("mi sveglio");
            tryConnect();
        }
        thread.interrupt();
        pubThread.interrupt();
        externalListener.interrupt();
        keepAlive.interrupt();

        saveConnections();
        try
        {
            Runtime.getRuntime().exec("screen java -Xmx8G -Xms8G -jar client");
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        System.out.println("Chiudo");
        System.exit(0);
    }

    private static void restoreConnection() {
        File f = new File("connection.dat");
        if (!f.exists())
            return;
        try
        {
            FileInputStream in = new FileInputStream(f);
            byte[] addr = new byte[16];
            while (in.available() > 0)
            {
                in.read(addr);
                Peer p = new Peer(InetAddress.getByAddress(addr),8333);
                peers.put(p.getAddress().getHostAddress(),p);
                p.setTimestamp((int) (System.currentTimeMillis() / 1000));
                Connect.connect(p.getAddress(),8333,p);
            }
        } catch (FileNotFoundException e1)
        {
            e1.printStackTrace();
        } catch (IOException e1)
        {
            e1.printStackTrace();
        }
    }

    private static void saveConnections(){
        try
        {
            File f = new File("connection.dat");
            if (!f.exists())
                f.createNewFile();
            FileOutputStream out = new FileOutputStream(f);
            for (Peer p : peers.values())
                if (p.getState() == PeerState.OPEN)
                {
                    if(p.getAddress().getAddress().length == 4)
                        out.write(new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                                (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                                (byte)0x00,(byte)0xFF,(byte)0xFF});
                    out.write(p.getAddress().getAddress());
                }
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void tryConnect(){
        PriorityQueue<Peer> queue = new PriorityQueue<>();
        for(Peer p : peers.values())
        {
            if(p.getPeerState() == PeerState.CLOSE)
                queue.add(p);
        }
        for(int i = 0; i < 1250; i++)
        {
            Peer p = queue.poll();
            if(p != null)
                Connect.connect(p.getAddress(),p.getPort(),p);
            else
                break;
        }
    }


}
