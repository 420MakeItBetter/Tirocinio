package Client;

import Client.messages.PeerAddress;
import Client.messages.Version;
import Client.Protocol.Connect;
import Client.network.Peer;
import Client.network.SocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Matteo on 07/10/2016.
 */
public class Main {



    public static Logger logger = LoggerFactory.getLogger(Main.class);

    public static SocketListener listener = new SocketListener();

    public static List<Peer> peers = new ArrayList<>();

    public static void main(String [] args) throws IOException {

        File addresses = new File("./addresses.dat");

        if(!addresses.exists())
        {
            logger.info("File addresses.dat created, is this the first time you start this Client?");
            addresses.createNewFile();
        }
        BufferedReader reader = new BufferedReader(new FileReader(addresses));

        String address;
        String port;
        while((address = reader.readLine()) != null)
        {
            port = reader.readLine();
            Peer p = new Peer(InetAddress.getByName(address),Integer.valueOf(port));
            peers.add(p);
        }

        Thread thread = new Thread(listener);
        thread.start();
        for(Peer p : peers)
        {
            startConnect(p.getAddress(),p.getPort());
        }
        for(String s : BitConstants.DNS)
        {
            InetAddress [] addrs = InetAddress.getAllByName(s);
            for (InetAddress addr : addrs)
            {
                startConnect(addr,BitConstants.PORT);
            }
        }


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
        v.setHeight(0);
        v.setRelay(true);

        SocketChannel channel = null;
        try
        {
            channel = Connect.connect(addr);
        } catch (IOException e)
        {
            logger.warn("{} irraggiungibile",addr);
            return false;
        }
        logger.info("Connesso a {}", addr);
        Peer p = new Peer(addr, BitConstants.PORT);
        Connect.sendVersion(v, channel, p);
        return true;
    }


}
