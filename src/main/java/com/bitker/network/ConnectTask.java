package com.bitker.network;

import com.bitker.Main;
import com.bitker.eventservice.EventService;
import com.bitker.eventservice.events.NotConnectedEvent;

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


    private SocketChannel skt;

    public ConnectTask(Peer p){
        this.p = p;
        skt = null;
        Main.listener.connectNumber.incrementAndGet();
    }

    @Override
    protected void clean() {
        Main.listener.connectNumber.decrementAndGet();
      //  System.err.println(Thread.currentThread().getName()+"finished");
    }

    @Override
    protected void closeResources() {
        try
        {
            if(skt != null)
                skt.close();
        } catch (IOException ignored)
        {}
        p.close();
        EventService.getInstance().publish(new NotConnectedEvent(p));
        Main.listener.connectNumber.decrementAndGet();
      //  System.err.println(Thread.currentThread().getName()+"abnormal finish");
    }

    @Override
    protected void doTask() throws IOException {
        try
        {
            if(p.getAddress().equals(InetAddress.getByName("131.114.88.218")))
                return;
        } catch (UnknownHostException ignored)
        {}
        //System.err.println(Thread.currentThread().getName()+"before open");
        skt = SocketChannel.open();
        //System.err.println(Thread.currentThread().getName()+"after open");
        skt.setOption(StandardSocketOptions.SO_REUSEADDR,true);
        //skt.socket().connect(new InetSocketAddress(p.getAddress(),p.getPort()),10000);
        //connections.incrementAndGet();
        skt.configureBlocking(false);
        skt.connect(new InetSocketAddress(p.getAddress(),p.getPort()));
        p.setTime();
        p.setSocket(skt);
        p.setIn(false);
        Main.listener.addChannel(skt,SelectionKey.OP_CONNECT,p);
    }
}
