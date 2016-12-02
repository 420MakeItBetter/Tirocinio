package Client.network;

import Client.Main;
import Client.messages.Version;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Matteo on 14/11/2016.
 */
public class ConnectTask implements Runnable {

    public static AtomicInteger connections = new AtomicInteger(0);

    Peer p;

    public ConnectTask(Peer p){
        this.p = p;
    }


    @Override
    public void run() {
        SocketChannel skt = null;
        try
        {
            if(p.getAddress().equals(InetAddress.getByName("131.114.88.218")))
                return;
        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        try
        {
            skt = SocketChannel.open();
            Main.listener.openedFiles.incrementAndGet();
        } catch (IOException e)
        {
            try
            {
                System.out.println(Main.listener.openedFiles.decrementAndGet());
                skt.close();
                e.printStackTrace();
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
            connections.incrementAndGet();
            skt.configureBlocking(false);
            p.setPeerState(PeerState.HANDSAKE);
            p.setSocket(skt);
            Main.listener.addChannel(skt, SelectionKey.OP_WRITE | SelectionKey.OP_READ,p);
            VersionTask t = new VersionTask(skt,p);
            Main.listener.ex.execute(t);
        } catch (IOException e)
        {
            e.printStackTrace();
            p.incrementAttempt();
            p.setPeerState(PeerState.CLOSE);
            Main.oldnotConnectedAdressess.add(p);
            try
            {
                System.out.println(Main.listener.openedFiles.decrementAndGet());
                skt.close();
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }

        }
    }
}
