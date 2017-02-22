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
public class ConnectTask extends Task {

    public static AtomicInteger connections = new AtomicInteger(0);


    SocketChannel skt;

    public ConnectTask(Peer p){
        this.p = p;
        skt = null;
    }

    @Override
    protected void clean() {

    }

    @Override
    protected void closeResources() {
        try
        {
            System.out.println(Main.listener.openedFiles.decrementAndGet());
            skt.close();
        } catch (IOException e1)
        {}
        p.close();
        Main.oldnotConnectedAdressess.add(p);
    }

    @Override
    protected void doTask() throws IOException {
        try
        {
            if(p.getAddress().equals(InetAddress.getByName("131.114.88.218")))
                return;
        } catch (UnknownHostException e)
        {}
        skt = SocketChannel.open();
        Main.listener.openedFiles.incrementAndGet();
        skt.setOption(StandardSocketOptions.SO_REUSEADDR,true);
        skt.socket().connect(new InetSocketAddress(p.getAddress(),p.getPort()),10000);
        connections.incrementAndGet();
        skt.configureBlocking(false);
        p.setPeerState(PeerState.HANDSAKE);
        p.setSocket(skt);
        Main.listener.addChannel(skt, SelectionKey.OP_WRITE | SelectionKey.OP_READ,p);
        VersionTask t = new VersionTask(skt,p);
        Main.listener.ex.execute(t);
    }
}
