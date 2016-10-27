package Client.network;

import Client.BitConstants;
import Client.Main;
import Client.Protocol.Connect;
import Client.messages.PeerAddress;
import Client.messages.Version;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.Random;

/**
 * Created by Matteo on 25/10/2016.
 */
public class VersionTask implements Runnable {

    private SocketChannel skt;
    private static final Random random = new Random();

    public VersionTask(SocketChannel skt){
        Main.listener.versionNumber.incrementAndGet();
        this.skt = skt;
    }

    @Override
    public void run() {
        PeerAddress my = new PeerAddress();
        try
        {
            my.setAddress(InetAddress.getByName("127.0.0.1"));
            my.setPort(BitConstants.PORT);
            my.setService(0);
            PeerAddress your = new PeerAddress();
            your.setAddress(((InetSocketAddress) skt.getRemoteAddress()).getAddress());
            your.setPort(((InetSocketAddress) skt.getRemoteAddress()).getPort());
            your.setService(0);
            Version v = new Version();
            v.setMyAddress(my);
            v.setYourAddress(your);
            v.setServices(0);
            v.setTimestamp(System.currentTimeMillis() / BitConstants.TIME);
            v.setNonce(random.nextLong());
            v.setVersion(BitConstants.VERSION);
            v.setUserAgent("TestClient.0.0.1");
            v.setHeight(BitConstants.LASTBLOCK);
            v.setRelay(true);
            Peer p = null;
            if(Main.peers.containsKey(your.getAddress()))
                p = Main.peers.get(your.getAddress());
            else
                p = new Peer(your.getAddress(), your.getPort());
            p.setPeerState(PeerState.HANDSAKE);
            Main.peers.put(p.getAddress(),p);
            Connect.sendVersion(v, skt, p);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        Main.listener.versionNumber.decrementAndGet();

    }
}
