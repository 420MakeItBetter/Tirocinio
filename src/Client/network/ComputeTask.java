package Client.network;

import Client.BitConstants;
import Client.Main;
import Client.Protocol.Connect;
import Client.Protocol.InventoryProtocol;
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
 *
 */
public class ComputeTask implements Runnable {

    private SocketChannel skt;
    private Peer p;
    private Message m;
    private Logger logger = LoggerFactory.getLogger(ReadTask.class);

    public ComputeTask(SocketChannel skt, Peer p,Message m){
        Main.listener.computeNumber.incrementAndGet();
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
                saveAddressees((Address) m);
            else if(m instanceof Inventory)
                inventoryStat((Inventory) m);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        Main.listener.computeNumber.decrementAndGet();
    }

    private void inventoryStat(Inventory m) {
        for(InventoryVector v : m.getInventoryVectors())
            switch (v.getType())
            {
                case ERROR:
                    Main.invStat.error.incrementAndGet();
                    break;
                case MSG_TX:
                    Main.invStat.transiction.incrementAndGet();
                    break;
                case MSG_BLOCK:
                    Main.invStat.block.incrementAndGet();
                    break;
                case MSG_CMPCT_BLOCK:
                    Main.invStat.cmpct_block.incrementAndGet();
                    break;
                case MSG_FILTERED_BLOCK:
                    Main.invStat.filtered_block.incrementAndGet();
                    break;
            }
        try
        {
            InventoryProtocol.sendInventory(m,skt,p);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }

    private void verackResponse() {
        p.setPeerState(PeerState.OPEN);
    }

    private void saveAddressees(Address m) throws IOException {
        for(PeerAddress p : m.getAddresses())
        {
            if(!Main.peers.containsKey(p.getAddress()))
                Connect.connect(p.getAddress(),p.getPort());
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
        p.setTimestamp((int) (System.currentTimeMillis() / BitConstants.TIME));
        p.setPort(m.getYourAddress().getPort());
        Connect.sendVerAck(ack,skt,p);
        Runnable r = new AddressGetter(skt,p);
        Main.listener.tasks.add(r);
    }






}
