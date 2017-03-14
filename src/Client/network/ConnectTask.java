package Client.network;

import Client.Main;
import Client.eventservice.EventService;
import Client.eventservice.events.ConnectedEvent;
import Client.eventservice.events.NotConnectedEvent;

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
    boolean tryagain;

    public ConnectTask(Peer p,boolean tryagain){
        this.p = p;
        this.tryagain = tryagain;
        skt = null;
    }

    @Override
    protected void clean() {

    }

    @Override
    protected void closeResources() {
        try
        {
            skt.close();
            Main.openedFiles.decrementAndGet();
        } catch (IOException e1)
        {}
        if(tryagain)
            if(p.close())
                Main.listener.ex.execute(new ConnectTask(p,false));
        else if(p.close())
                Main.oldnotConnectedAdressess.add(p);
        EventService.getInstance().publish(new NotConnectedEvent(p));
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
        Main.openedFiles.incrementAndGet();
        skt.setOption(StandardSocketOptions.SO_REUSEADDR,true);
        skt.socket().connect(new InetSocketAddress(p.getAddress(),p.getPort()),10000);
        connections.incrementAndGet();
        skt.configureBlocking(false);
        p.setPeerState(PeerState.HANDSHAKE);
        p.setSocket(skt);
        p.setIn(false);
        Main.listener.addChannel(skt,SelectionKey.OP_READ,p);
        VersionTask t = new VersionTask(skt,p);
        Main.listener.ex.execute(t);


        EventService.getInstance().publish(new ConnectedEvent(p));
    }
}
