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

    public static Peer followed = null;

    public static Logger logger = LoggerFactory.getLogger(Main.class);

    public static SocketListener listener = new SocketListener();

    public static PriorityBlockingQueue<Peer> newnotConnectedAdressess = new PriorityBlockingQueue<>();

    public static PriorityBlockingQueue<Peer> oldnotConnectedAdressess = new PriorityBlockingQueue<>();

    public static PriorityBlockingQueue<Peer> oldalreadyConnectedAdressess = new PriorityBlockingQueue<>();

    public static ConcurrentHashMap<String, Peer> peers = new ConcurrentHashMap<>();

    public static InventoryStat invStat = new InventoryStat();

    public static CommanderListener commandListener = new CommanderListener();

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
        try
        {
            Peer p = new Peer(InetAddress.getByName("176.10.116.242"),8333);
            p.setTimestamp((int) (System.currentTimeMillis()/1000));
            peers.put(p.getAddress().getHostAddress(),p);
            newnotConnectedAdressess.add(p);
        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        String str = "47.89.179.113\n" +
                "47.89.182.29\n" +
                "47.89.186.32\n" +
                "47.89.188.98\n" +
                "52.11.241.197\n" +
                "50.7.71.172\n" +
                "82.1.197.255\n" +
                "52.18.56.236\n" +
                "198.100.144.20\n" +
                "129.13.252.36\n" +
                "212.159.19.195\n" +
                "142.4.215.155\n" +
                "85.10.202.167\n" +
                "148.251.151.71\n" +
                "52.28.19.80\n" +
                "72.24.124.250\n" +
                "54.207.80.204\n" +
                "192.254.76.218\n" +
                "54.186.75.87\n" +
                "129.13.252.47\n" +
                "52.207.223.193\n" +
                "212.72.194.20\n" +
                "162.221.202.230\n" +
                "54.229.105.178\n" +
                "52.198.193.111\n" +
                "195.72.211.118\n" +
                "112.124.23.59\n" +
                "37.130.227.133\n" +
                "151.80.178.25\n" +
                "114.55.29.152\n" +
                "91.220.131.39\n" +
                "67.149.141.90\n" +
                "188.113.84.116\n" +
                "217.23.6.166\n" +
                "128.211.184.6\n" +
                "5.189.136.83\n" +
                "54.84.171.183\n" +
                "52.78.135.180\n" +
                "104.197.47.89\n" +
                "52.1.61.160\n" +
                "176.123.7.148\n" +
                "91.203.146.90\n" +
                "209.11.180.182\n" +
                "185.81.157.110\n" +
                "78.47.244.4\n" +
                "188.40.38.83\n" +
                "212.183.213.228\n" +
                "185.81.157.110\n" +
                "104.163.144.6\n" +
                "45.33.65.130\n" +
                "198.23.30.231\n" +
                "23.96.218.65\n" +
                "73.234.159.4\n" +
                "62.75.130.161\n" +
                "192.42.116.16\n" +
                "46.4.3.125\n" +
                "146.57.248.225\n" +
                "87.132.38.157\n" +
                "111.207.165.188\n" +
                "191.235.87.191\n" +
                "92.221.176.203\n" +
                "50.197.11.193\n" +
                "70.77.62.180\n" +
                "101.201.53.37\n" +
                "87.218.38.113\n" +
                "98.226.150.6\n" +
                "213.239.211.6\n" +
                "65.24.58.196\n" +
                "136.243.139.120\n" +
                "184.0.116.194\n" +
                "94.23.4.25\n" +
                "35.156.94.60\n" +
                "70.77.73.54\n" +
                "91.138.178.91\n" +
                "95.57.91.24\n" +
                "45.32.22.99\n" +
                "146.148.120.40\n" +
                "62.118.139.13\n" +
                "52.59.231.232\n" +
                "78.38.176.2\n" +
                "35.187.3.235\n" +
                "23.251.134.107\n" +
                "92.50.118.2\n" +
                "79.219.108.158\n" +
                "185.81.157.110\n" +
                "78.21.113.125\n" +
                "69.61.25.60\n" +
                "138.201.18.26\n" +
                "198.23.30.231\n" +
                "85.17.31.90\n" +
                "95.211.188.229\n" +
                "80.229.154.15\n" +
                "184.161.207.72\n" +
                "84.227.124.142\n" +
                "114.55.29.227\n" +
                "69.255.53.10\n" +
                "185.81.157.110\n" +
                "83.199.132.10\n" +
                "72.36.89.11\n" +
                "141.55.229.170\n" +
                "176.41.171.209\n" +
                "99.104.75.58\n" +
                "138.197.197.132\n" +
                "138.197.194.32\n" +
                "138.197.195.52\n" +
                "138.197.203.66\n" +
                "138.197.197.108\n" +
                "138.197.201.197\n" +
                "138.197.194.32\n" +
                "138.197.197.164\n" +
                "138.197.197.152\n" +
                "138.197.203.86\n" +
                "138.197.197.132\n" +
                "138.197.197.174\n" +
                "138.197.197.179\n" +
                "138.197.197.164\n" +
                "138.197.195.52\n" +
                "138.197.203.86\n" +
                "138.197.198.120\n" +
                "138.197.197.108\n" +
                "138.197.194.32\n" +
                "138.68.10.138\n" +
                "138.197.197.174\n" +
                "138.197.197.50\n" +
                "138.197.195.32\n" +
                "138.197.198.120\n" +
                "138.197.195.52\n" +
                "138.197.201.197\n" +
                "138.197.197.50\n" +
                "138.197.197.108\n" +
                "138.197.197.152\n" +
                "138.197.203.66\n" +
                "138.197.195.32\n" +
                "138.197.197.152\n" +
                "138.197.197.132\n" +
                "138.197.203.86\n" +
                "138.197.197.174\n" +
                "138.68.10.138\n" +
                "138.197.197.50\n" +
                "138.197.197.179\n" +
                "138.197.197.164\n" +
                "138.68.10.138\n" +
                "138.197.201.197\n" +
                "138.197.198.120\n" +
                "138.197.197.179\n" +
                "138.197.203.66\n" +
                "138.197.195.32\n" +
                "76.180.172.123\n" +
                "131.114.88.218\n" +
                "143.176.92.131\n" +
                "79.152.8.106\n" +
                "90.173.135.190\n" +
                "76.67.54.133\n" +
                "52.205.213.45\n" +
                "201.254.155.243\n" +
                "54.91.148.239";
        for(String s : str.split("\n"))
        {
            Peer p;
            try
            {
                p = new Peer(InetAddress.getByName(s),8333);
                p.setTimestamp((int) (System.currentTimeMillis()/1000));
                peers.put(p.getAddress().getHostAddress(),p);
                newnotConnectedAdressess.add(p);
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
                    Connect.connect(p.getAddress(),p.getPort(),p);

            }
            else
            {
                if(counter < 1000)
                {
                    Peer p = oldalreadyConnectedAdressess.poll();
                    if(p == null)
                        p = oldnotConnectedAdressess.poll();
                    if (p != null)
                        Connect.connect(p.getAddress(), p.getPort(), p);

                }
                else
                {
                    Peer p = oldnotConnectedAdressess.poll();
                    if(p != null)
                        Connect.connect(p.getAddress(),p.getPort(),p);
                }
            }
        }

    }

}
