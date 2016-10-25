package Client;

import Client.messages.PeerAddress;
import Client.messages.Version;
import Client.Protocol.Connect;
import Client.network.Peer;
import Client.network.PeerState;
import Client.network.SocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Matteo on 07/10/2016.
 *
 */
public class Main {


    public static boolean showLog=true;

    public static Logger logger = LoggerFactory.getLogger(Main.class);

    public static SocketListener listener = new SocketListener();

    public static ConcurrentHashMap<InetAddress, Peer> peers = new ConcurrentHashMap<>();


    public static void main(String [] args) throws IOException {

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
            addresses.createNewFile();
        }
        BufferedReader reader = new BufferedReader(new FileReader(addresses));

        String address;
        String port;
        String timestamp;
        while((address = reader.readLine()) != null)
        {
            port = reader.readLine();
            timestamp = reader.readLine();
            Peer p = new Peer(InetAddress.getByName(address),Integer.valueOf(port));
            p.setTimestamp(Integer.valueOf(timestamp));
            peers.put(p.getAddress(),p);
        }

        Thread thread = new Thread(listener);
        thread.start();
        Thread mainThread = new Thread(new MainThread(thread));
        mainThread.start();
        for(Peer p : peers.values())
        {
            startConnect(p.getAddress(),p.getPort());
        }
        /*for(String s : BitConstants.DNS)
        {
            InetAddress [] addrs = InetAddress.getAllByName(s);
            for (InetAddress addr : addrs)
            {
                if(startConnect(addr,BitConstants.PORT))
                    break;
            }
            break;
        }
        */



    }

    private static boolean startConnect(InetAddress addr,int port) throws IOException {
        PeerAddress my = new PeerAddress();
        my.setAddress(InetAddress.getByName("127.0.0.1"));
        my.setPort(BitConstants.PORT);
        my.setService(0);
        PeerAddress your = new PeerAddress();
        your.setAddress(addr);
        your.setPort(port);
        your.setService(0);
        Version v = new Version();
        v.setMyAddress(my);
        v.setYourAddress(your);
        v.setServices(0);
        v.setTimestamp(System.currentTimeMillis() / BitConstants.TIME);
        v.setNonce(new Random().nextLong());
        v.setVersion(BitConstants.VERSION);
        v.setUserAgent("TestClient.0.0.1");
        v.setHeight(BitConstants.LASTBLOCK);
        v.setRelay(true);

        SocketChannel channel = null;
        try
        {
            channel = Connect.connect(addr);
        } catch (IOException e)
        {
            if(showLog)
                logger.warn("{} irraggiungibile",addr);
            return false;
        }
        if(showLog)
            logger.info("Connesso a {}", addr);
        Peer p = null;
        if(peers.containsKey(addr))
            p = peers.get(addr);
        else
            p = new Peer(addr, BitConstants.PORT);
        p.setPeerState(PeerState.HANDSAKE);
        peers.put(addr,p);
        Connect.sendVersion(v, channel, p);
        return true;
    }

}
