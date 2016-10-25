package Client.network;

import Client.Protocol.Connect;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

/**
 * Created by machiara on 21/10/16.
 *
 */
public class AddressGetter implements Runnable {

    SocketChannel skt;
    Peer p;

    public AddressGetter(SocketChannel skt, Peer p) {
        this.skt = skt;
        this.p = p;
    }

    @Override
    public void run() {
        try {
            Connect.sendGetAddress(skt,p);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }

    }
}
