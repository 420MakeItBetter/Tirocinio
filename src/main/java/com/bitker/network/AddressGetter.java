package com.bitker.network;

import com.bitker.Main;
import com.bitker.protocol.Connect;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

/**
 * Created by machiara on 21/10/16.
 *
 */
public class AddressGetter implements Runnable {

    private SocketChannel skt;
    private Peer p;
    private int time;

    public AddressGetter(SocketChannel skt, Peer p) {
        Main.listener.addressGetter.incrementAndGet();
        this.skt = skt;
        this.p = p;
        time = 1;
    }

    public AddressGetter(SocketChannel skt, Peer p,int time) {
        Main.listener.addressGetter.incrementAndGet();
        this.skt = skt;
        this.p = p;
        this.time = time;
    }

    @Override
    public void run() {
        try{
            Connect.sendGetAddress(skt, p);
        } catch (ClosedChannelException | InterruptedException e) {
            e.printStackTrace();
        }
        Main.listener.addressGetter.decrementAndGet();
    }
}
