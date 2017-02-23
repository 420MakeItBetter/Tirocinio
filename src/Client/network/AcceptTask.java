package Client.network;

import Client.Main;
import com.sun.org.apache.bcel.internal.generic.Select;

import java.io.IOException;
import java.net.StandardSocketOptions;
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
            skt.setOption(StandardSocketOptions.SO_REUSEADDR,true);
            skt.configureBlocking(false);
            Peer peer = null;
            if(Main.peers.containsKey(skt.socket().getInetAddress().getHostAddress()))
            {
                peer = Main.peers.get(skt.socket().getInetAddress().getHostAddress());
                if(peer.getState() != PeerState.CLOSE)
                {
                    skt.close();
                    return;
                }
                Main.oldalreadyConnectedAdressess.remove(peer);
                Main.oldnotConnectedAdressess.remove(peer);
                Main.newnotConnectedAdressess.remove(peer);
            }
            else
            {
                peer = new Peer(skt.socket().getInetAddress(), 8333);
                Main.peers.put(peer.getAddress().getHostAddress(), peer);
            }
            peer.setPeerState(PeerState.HANDSAKE);
            peer.setSocket(skt);
            Main.listener.addChannel(skt, SelectionKey.OP_WRITE | SelectionKey.OP_READ,peer);
            VersionTask v = new VersionTask(skt,peer);
            Main.listener.ex.execute(v);
        } catch (IOException e)
        {
            e.printStackTrace();
            try
            {
                skt.close();
                System.out.println(Main.listener.openedFiles.decrementAndGet());
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }
        Main.listener.acceptNumber.decrementAndGet();
    }
}
