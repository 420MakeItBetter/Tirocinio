package com.bitker;


import com.bitker.api.PublicInterface;
import com.bitker.network.*;
import com.bitker.protocol.Connect;
import com.bitker.remotelistener.ExternalListener;
import com.bitker.utils.BitConstants;


import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * Main class of the BitKer client, starts the various parts of the client then
 * tries to connect to new peer
 *
 * @author Matteo Franceschi mfranceschi94@gmail.com
 */
public class Main {

    /**
     *
     */
    public static Socket client = null;

    public static Peer followed = null;

    /**
     * This hashmap contains al the Peers that this client knows, it's accessed
     * by the ip address of the peer
     */
    public static ConcurrentHashMap<String, Peer> peers = new ConcurrentHashMap<>();

    /**
     * Mantains data about the statistics of the client
     */
    public static InventoryStat invStat = new InventoryStat();

    /**
     * The number of opened files, used for debug purpose
     */
    public static AtomicLong openedFiles = new AtomicLong(0);

    /**
     * The class that manages all the I/O between this client and the Bitcoin network
     */
    public static SocketListener listener = new SocketListener();

    /**
     * The class that manages the I/O of the public interfae of this client
     */
    public static PublicInterface publicInterface = new PublicInterface();
    /**
     * Indicates if this client has started the procedure of termination
     */
    public static AtomicBoolean terminate = new AtomicBoolean(false);
	public static AtomicLong inventoryNumber = new AtomicLong(0);
    public static AtomicLong sentMessage = new AtomicLong(0);


    /**
     * Static method and entry point of the BitKer. This method first starts all the
     * threads, then tries to connect to 1250 new IP every minute.
     * It also set the output stream and error stream to 2 files so the entire log of
     * Bitker can be accessed.
     * @param args not used
     */
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
        /*
            Request to the Bitcoin DNS server to request a list of IP address
         */
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

        /*
            Create and start all the thread
         */
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
        /*
            Every minutes choose 1250 not connected IP and tries to connect to them
         */
        try
        {
            while (!terminate.get())
            {
                System.out.println("dormo");
                try
                {
                    Thread.currentThread().sleep(1000 * 60);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                if (listener.connectNumber.get() > 0)
                    continue;
                System.out.println("mi sveglio");
                tryConnect();
            }
        }catch (Exception e)
        {}
        saveConnections();
        System.out.println("Connessioni salvate");
        System.out.println("Chiudo");
        System.exit(0);
        /*
        thread.interrupt();
        pubThread.interrupt();
        externalListener.interrupt();
        keepAlive.interrupt();



        try
        {
            Runtime.getRuntime().exec("screen java -Xmx8G -Xms8G -jar client");
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        */

    }

    /**
     * This method put into the {@link ConcurrentHashMap} of peers the IP addresses saved
     * during the last run of the BitKer client
     */
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

    /**
     * This method saves into the file connection.dat all the address
     * of the OPEN connection
     */
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

    /**
     * This method first orders the not connected peers then try to connect
     * to the first 1250.
     */
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
