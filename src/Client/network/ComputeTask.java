package Client.network;

import Client.BitConstants;
import Client.Main;
import Client.Protocol.Connect;
import Client.Protocol.KeepAlive;
import Client.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.Random;

/**
 * Created by Matteo on 12/10/2016.
 */
public class ComputeTask implements Runnable {

    private SocketChannel skt;
    private Peer p;
    private Message m;
    private Logger logger = LoggerFactory.getLogger(ReadTask.class);

    public ComputeTask(SocketChannel skt, Peer p,Message m){
        this.skt = skt;
        this.p = p;
        this.m = m;
    }

    @Override
    public void run() {

        try
        {
            p.setTimestamp((int) (System.currentTimeMillis()/BitConstants.TIME));
            if(Main.showLog)
                logger.info("Messaggio ricevuto {} da {}",m.getCommand(),p.getAddress());
            if(m instanceof VerAck)
                verackResponse();
            else if(m instanceof Version)
                versionResponse((Version) m);
            else if(m instanceof Ping)
                pingResponse((Ping) m);
            else if(m instanceof Address)
                saveAddressess((Address) m);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void verackResponse() {
        p.setPeerState(PeerState.OPEN);
    }

    private void saveAddressess(Address m) throws IOException {
        PeerAddress my = new PeerAddress();
        my.setService(0);
        my.setPort(8333);
        my.setAddress(InetAddress.getByName("127.0.0.1"));
        for(PeerAddress p : m.getAddresses())
        {
            if(!Main.peers.containsKey(p.getAddress()))
            {
                Peer peer = new Peer(p.getAddress(),p.getPort());
                peer.setService(p.getService());
                peer.setTimestamp(p.getTime());
                Main.peers.put(p.getAddress(),peer);
                Version v = new Version();
                v.setMyAddress(my);
                v.setYourAddress(p);
                v.setServices(0);
                v.setTimestamp(System.currentTimeMillis()/BitConstants.TIME);
                v.setNonce(new Random().nextLong());
                v.setVersion(BitConstants.VERSION);
                v.setUserAgent("TestClient.0.0.1");
                v.setHeight(0);
                v.setRelay(true);
                SocketChannel skt = null;
                try{
                    skt = Connect.connect(p.getAddress(),p.getPort());
                }catch (IOException e)
                {
                    continue;
                }
                peer.setPeerState(PeerState.HANDSAKE);
                Connect.sendVersion(v,skt,peer);
            }
            else
            {
                Peer peer = Main.peers.get(p.getAddress());
                if(peer.getTimestamp() < p.getTime())
                    peer.setTimestamp(p.getTime());
            }
        }
    }

    private void pingResponse(Ping m) throws IOException {
        KeepAlive.sendPong(m,skt,p);
    }

    private void versionResponse(Version m) throws ClosedChannelException {
        VerAck ack = new VerAck();
        p.setPeerState(PeerState.OPEN);
        p.setService(m.getService());
        p.setTimestamp((int) (System.currentTimeMillis()/ BitConstants.TIME));
        p.setPort(m.getYourAddress().getPort());
        Connect.sendVerAck(ack,skt,p);
    }






}
