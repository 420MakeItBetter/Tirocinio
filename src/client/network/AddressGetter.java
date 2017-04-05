package client.network;

import client.Main;
import client.protocol.Connect;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

/**
 * Created by machiara on 21/10/16.
 *
 */
public class AddressGetter implements Runnable {

    SocketChannel skt;
    Peer p;
    int time;

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
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        Main.listener.addressGetter.decrementAndGet();
    }
}
