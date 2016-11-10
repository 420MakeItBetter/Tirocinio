package Client.network;

import Client.Main;
import com.sun.org.apache.bcel.internal.generic.Select;

import java.io.IOException;
import java.nio.channels.*;

/**
 * Created by Matteo on 11/10/2016.
 *
 */
public class AcceptTask implements Runnable{

    SocketChannel skt;

    public AcceptTask(SocketChannel skt) {
        Main.listener.acceptNumber.incrementAndGet();
        this.skt = skt;
    }


    @Override
    public void run() {
        try
        {
            skt.configureBlocking(false);
            Peer peer = null;
            if(Main.peers.containsKey(skt.socket().getInetAddress().getHostAddress()))
                peer = Main.peers.get(skt.socket().getInetAddress().getHostAddress());
            else
            {
                peer = new Peer(skt.socket().getInetAddress(), 8333);
                Main.peers.put(peer.getAddress().getHostAddress(), peer);
            }
            peer.setPeerState(PeerState.HANDSAKE);
            VersionTask v = new VersionTask(skt,peer);
            Main.listener.ex.execute(v);
        } catch (IOException e)
        {

            e.printStackTrace();
        }
        Main.listener.acceptNumber.decrementAndGet();
    }
}
