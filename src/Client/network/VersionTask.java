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
    private Peer p;
    private static final Random random = new Random();

    public VersionTask(SocketChannel skt,Peer p){
        Main.listener.versionNumber.incrementAndGet();
        this.skt = skt;
        this.p = p;
    }

    @Override
    public void run() {
        PeerAddress my = new PeerAddress();
        try
        {
            my.setAddress(InetAddress.getByName("131.114.88.218"));
            my.setPort(BitConstants.PORT);
            my.setService(1);
            PeerAddress your = new PeerAddress();
            your.setAddress(p.getAddress());
            your.setPort(p.getPort());
            your.setService(p.getService());
            Version v = new Version();
            v.setMyAddress(my);
            v.setYourAddress(your);
            v.setServices(1);
            v.setTimestamp(System.currentTimeMillis() / BitConstants.TIME);
            v.setNonce(random.nextLong());
            if(!p.getVersion())
                v.setVersion(BitConstants.VERSION);
            else
                v.setVersion(60001);
            v.setUserAgent("/TestClient.0.0.1/");
            v.setHeight(BitConstants.LASTBLOCK);
            v.setRelay(true);
            p.setPeerState(PeerState.HANDSAKE);
            Connect.sendVersion(v, skt, p);
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        Main.listener.versionNumber.decrementAndGet();

    }
}
