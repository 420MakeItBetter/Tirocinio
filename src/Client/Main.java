package Client;

import Client.messages.PeerAddress;
import Client.messages.Version;
import Client.Protocol.Connect;
import Client.network.InventoryStat;
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


    public static boolean showLog=false;

    public static Logger logger = LoggerFactory.getLogger(Main.class);

    public static SocketListener listener = new SocketListener();

    public static ConcurrentHashMap<InetAddress, Peer> peers = new ConcurrentHashMap<>();

    public static InventoryStat invStat = new InventoryStat();

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
            try
            {
                Connect.connect(p.getAddress(), p.getPort());
            }catch (IOException e)
            {}
        }
        for(String s : BitConstants.DNS)
        {
            InetAddress [] addrs = InetAddress.getAllByName(s);
            for (InetAddress addr : addrs)
            {
                if(!peers.containsKey(addr))
                    try
                    {
                        Connect.connect(addr);
                    }catch (IOException e)
                    {}
            }
        }




    }

}
