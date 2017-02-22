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
public class VersionTask extends Task {

    private SocketChannel skt;
    private static final Random random = new Random();

    public VersionTask(SocketChannel skt,Peer p){
        this.skt = skt;
        this.p = p;
    }

    @Override
    protected void clean() {

    }

    @Override
    protected void closeResources() {
        p.close();
        Main.oldalreadyConnectedAdressess.add(p);
        try {
            skt.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void doTask() throws IOException {
        PeerAddress my = new PeerAddress();
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
        if(p.getVersion())
            v.setVersion(BitConstants.VERSION);
        else
            v.setVersion(60001);
        v.setUserAgent("/TestClient.0.0.1/");
        v.setHeight(BitConstants.LASTBLOCK);
        v.setRelay(true);
        p.setPeerState(PeerState.HANDSAKE);
        try {
            Connect.sendVersion(v, skt, p);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
