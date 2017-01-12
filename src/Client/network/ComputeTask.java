package Client.network;

import Client.BitConstants;
import Client.CommanderListener;
import Client.Main;
import Client.Protocol.Connect;
import Client.Protocol.InventoryProtocol;
import Client.Protocol.KeepAlive;
import Client.commands.AddrStruct;
import Client.messages.*;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Matteo on 12/10/2016.
 *
 */
public class ComputeTask implements Runnable {

    private SocketChannel skt;
    private Peer p;
    private Message m;

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
            else if(m instanceof GetAddress)
                sendAddress((GetAddress) m);
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (NullPointerException e)
        {}
        Main.listener.computeNumber.decrementAndGet();
    }

    private void sendAddress(GetAddress m) {
        try
        {
            Connect.sendAddresses(skt,p);
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    private void inventoryStat(Inventory m) {
        for(InventoryVector v : m.getInventoryVectors())
            try
            {
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
            }catch (NullPointerException e)
            {
                e.printStackTrace();
            }
    }

    private void verackResponse() {p.setPeerState(PeerState.OPEN);}

    private void saveAddressees(Address m) throws IOException {

        for(PeerAddress p : m.getAddresses())
        {
            if(!Main.peers.containsKey(p.getAddress().getHostAddress()))
            {
                Peer peer = new Peer(p.getAddress(),p.getPort());
                peer.setTimestamp(p.getTime());
                peer.setService(p.getService());
                Main.peers.put(p.getAddress().getHostAddress(),peer);
                Main.newnotConnectedAdressess.add(peer);
            }
            else
            {
                Peer peer = Main.peers.get(p.getAddress().getHostAddress());
                if(peer.getTimestamp() < p.getTime())
                    peer.setTimestamp(p.getTime());
            }

        }
    }

    private void pingResponse(Ping m) throws IOException, InterruptedException {
        KeepAlive.sendPong(m,skt,p);
    }

    private void versionResponse(Version m) throws ClosedChannelException, InterruptedException {
        AtomicInteger number = Main.userAgents.get(m.getUserAgent());
        p.setPeerState(PeerState.OPEN);
        if(number == null)
            number = new AtomicInteger();
        AtomicInteger t = Main.userAgents.put(m.getUserAgent(),number);
        if(t != null)
        {
            Main.userAgents.put(m.getUserAgent(), t);
            number = t;
        }
        number.incrementAndGet();
        VerAck ack = new VerAck();
        p.setPeerState(PeerState.OPEN);
        p.setService(m.getService());
        p.setTimestamp((int) (System.currentTimeMillis() / BitConstants.TIME));
        p.setPort(m.getYourAddress().getPort());
        p.setAgent(m.getUserAgent());
        Connect.sendVerAck(ack,skt,p);
        Runnable r = new AddressGetter(skt, p, 10);
        Main.listener.tasks.add(r);

    }






}
