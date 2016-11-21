package Client;

import Client.messages.PeerAddress;
import Client.messages.SerializedMessage;
import Client.messages.Version;
import Client.Protocol.Connect;
import Client.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Matteo on 07/10/2016.
 *
 */
public class Main {

    public static ConcurrentHashMap<String, AtomicInteger> userAgents = new ConcurrentHashMap<>();

    public static boolean showLog=false;

    public static Logger logger = LoggerFactory.getLogger(Main.class);

    public static SocketListener listener = new SocketListener();

    public static PriorityBlockingQueue<Peer> newnotConnectedAdressess = new PriorityBlockingQueue<>();

    public static PriorityBlockingQueue<Peer> oldnotConnectedAdressess = new PriorityBlockingQueue<>();

    public static ConcurrentHashMap<String, Peer> peers = new ConcurrentHashMap<>();

    public static InventoryStat invStat = new InventoryStat();

    public static void main(String [] args) {

        File addresses = new File("./addresses.dat");
        if(args.length > 0)
        {
            if(args[0].equals("-s"))
                showLog=true;
        }


        if(!addresses.exists())
        {
            if(showLog)
                logger.info("File addresses.dat created, is this the first time you start this Client?");
            try
            {
                addresses.createNewFile();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(addresses));
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        String address;
        String port;
        String timestamp;
        try
        {
            while((address = reader.readLine()) != null)
            {
                port = reader.readLine();
                timestamp = reader.readLine();
                Peer p = new Peer(InetAddress.getByName(address),Integer.valueOf(port));
                p.setTimestamp(Integer.valueOf(timestamp));
                newnotConnectedAdressess.add(p);
                peers.put(p.getAddress().getHostAddress(),p);
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
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
        SerializedMessage.initializeBuffers();
        Thread thread = new Thread(listener);
        thread.start();
        Thread mainThread = new Thread(new MainThread());
        mainThread.start();
        Thread externalListener = new Thread(new ExternalListener());
        externalListener.start();
        Thread commanderListener = new Thread(new CommanderListener());
        commanderListener.start();
        Thread keepAlive = new Thread(new KeepAliveTask());
        keepAlive.start();

        int counter = 0;
        while(true)
        {
            counter++;
            if(counter < 750)
            {
                Peer p = newnotConnectedAdressess.poll();
                if(p == null)
                    p = oldnotConnectedAdressess.poll();
                if(p != null)
                    Connect.connect(p.getAddress(),p.getPort(),p);

            }
            else
            {
                if(counter == 1000)
                {
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
                Peer p = oldnotConnectedAdressess.poll();
                if(p != null)
                    Connect.connect(p.getAddress(),p.getPort(),p);


            }


        }

    }

}
