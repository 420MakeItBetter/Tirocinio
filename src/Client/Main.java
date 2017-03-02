package Client;

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

/**
 * Created by Matteo on 07/10/2016.
 *
 */
public class Main {

    public static Socket client = null;

    public static boolean showLog=false;

    public static Peer followed = null;

    public static SocketListener listener = new SocketListener();

    public static PriorityBlockingQueue<Peer> newnotConnectedAdressess = new PriorityBlockingQueue<>();

    public static PriorityBlockingQueue<Peer> oldnotConnectedAdressess = new PriorityBlockingQueue<>();

    public static PriorityBlockingQueue<Peer> oldalreadyConnectedAdressess = new PriorityBlockingQueue<>();

    public static ConcurrentHashMap<String, Peer> peers = new ConcurrentHashMap<>();

    public static InventoryStat invStat = new InventoryStat();

    public static CommanderListener commandListener = new CommanderListener();

    public static LinkedList<PeerAddress> addressesList = new LinkedList<>();

    public static void main(String [] args) {

        File addresses = new File("./addresses.dat");
        if(args.length > 0)
        {
            if(args[0].equals("-s"))
                showLog=true;
        }


        if(!addresses.exists())
        {
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

        for(int i = 83; i <= 88; i++)
        {
            if(addressesList.size() == 100000)
                break;
            if(84 <= i && i <= 85)
                continue;
            for(int j = 0; j <= 255; j++)
            {
                InetAddress address = null;
                try
                {
                    address = InetAddress.getByName("131.114." + String.valueOf(i) + "." + String.valueOf(j));
                } catch (UnknownHostException e)
                {
                    e.printStackTrace();
                }
                for(int k = 8000; k < 9000; k++)
                {
                    if (addressesList.size() == 100000)
                        break;
                    if (i == 88 && j == 218)
                        continue;
                    PeerAddress addr = new PeerAddress();
                    addr.setService(1);
                    addr.setTime((int) (System.currentTimeMillis() / 1000) - 60 * 10);
                    addr.setPort(k);
                    addr.setAddress(address);
                    addressesList.add(addr);
                    System.out.println(addr.getAddress().getHostAddress());
                }
                if(addressesList.size() == 100000)
                    break;
            }
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

        File agents = new File("./agents");
        if(!agents.exists())
            try
            {
                agents.createNewFile();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            FileWriter writer = null;
        try
        {
            writer = new FileWriter(agents);
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

        Thread thread = new Thread(listener);
        thread.start();
        Thread mainThread = new Thread(new MainThread());
        mainThread.start();
        Thread externalListener = new Thread(new ExternalListener());
        externalListener.start();
        Thread commanderListener = new Thread(commandListener);
        commanderListener.start();
        Thread keepAlive = new Thread(new KeepAliveTask());
        keepAlive.start();

        int counter = 0;
        while(true)
        {
            counter++;
            if(counter == 1250)
            {
                counter = 0;
                long time = System.currentTimeMillis();

                System.out.println("Start");
                for(Map.Entry<String, Peer> entry : peers.entrySet())
                {
                    if(System.currentTimeMillis() - time >= 1000*60*5)
                        break;
                    if (entry.getValue().getState() == PeerState.CLOSE && entry.getValue().getAttempt() > 1)
                    {
                        if(oldalreadyConnectedAdressess.contains(entry.getValue()))
                        {
                            if (entry.getValue().getAttempt() > 2)
                            {
                                entry.getValue().close();
                                peers.remove(entry.getKey());
                                oldalreadyConnectedAdressess.remove(entry.getValue());
                                oldnotConnectedAdressess.remove(entry.getValue());
                                newnotConnectedAdressess.remove(entry.getValue());
                            }
                        }
                        else
                        {
                            entry.getValue().close();
                            peers.remove(entry.getKey());
                            oldalreadyConnectedAdressess.remove(entry.getValue());
                            oldnotConnectedAdressess.remove(entry.getValue());
                            newnotConnectedAdressess.remove(entry.getValue());
                        }
                    }
                }
                long t = System.currentTimeMillis() - time;
                System.out.println("Done in: "+t/1000+"s");
                if(t < 1000*60)
                    try
                    {
                        System.out.println("dormo");
                        Thread.currentThread().sleep(1000 * 60 - t);
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
                    p.close();
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
                        p.close();
                        Connect.connect(p.getAddress(), p.getPort(), p);
                    }
                }
                else
                {
                    Peer p = oldnotConnectedAdressess.poll();
                    if(p != null)
                    {
                        p.close();
                        Connect.connect(p.getAddress(),p.getPort(),p);
                    }
                }
            }
        }

    }

}
