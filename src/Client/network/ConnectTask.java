package Client.network;

import Client.Main;
import Client.messages.Version;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by Matteo on 14/11/2016.
 */
public class ConnectTask implements Runnable {

    Peer p;

    public ConnectTask(Peer p){
        this.p = p;
    }


    @Override
    public void run() {
        SocketChannel skt = null;
        try
        {
            skt = SocketChannel.open();
            Main.listener.openedFiles.incrementAndGet();
        } catch (IOException e)
        {
            try
            {
                Main.listener.openedFiles.decrementAndGet();
                skt.close();
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }
            p.incrementAttempt();
            p.setPeerState(PeerState.CLOSE);
            Main.oldnotConnectedAdressess.add(p);
            e.printStackTrace();
            return;
        }
        try
        {
            skt.setOption(StandardSocketOptions.SO_REUSEADDR,true);
            skt.socket().connect(new InetSocketAddress(p.getAddress(),p.getPort()),10000);
            skt.configureBlocking(false);
            p.setPeerState(PeerState.HANDSAKE);
            p.setSocket(skt);
            Main.listener.addChannel(skt, SelectionKey.OP_WRITE | SelectionKey.OP_READ,p);
            VersionTask t = new VersionTask(skt,p);
            Main.listener.ex.execute(t);
        } catch (IOException e)
        {
            p.incrementAttempt();
            p.setPeerState(PeerState.CLOSE);
            Main.oldnotConnectedAdressess.add(p);
            try
            {
                Main.listener.openedFiles.decrementAndGet();
                skt.close();
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }

        }
    }
}
