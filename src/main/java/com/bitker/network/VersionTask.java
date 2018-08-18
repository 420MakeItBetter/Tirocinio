package com.bitker.network;

import com.bitker.utils.BitConstants;
import com.bitker.Main;
import com.bitker.protocol.Connect;
import com.bitkermessage.client.messages.messages.PeerAddress;
import com.bitkermessage.client.messages.messages.Version;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.Random;

/**
 * Created by Matteo on 25/10/2016.
 */
public class VersionTask extends Task {

    private SocketChannel skt;
    private static final Random random = new Random();

    public VersionTask(SocketChannel skt,Peer p){
        this.skt = skt;
        this.p = p;
        Main.listener.versionNumber.incrementAndGet();
    }

    @Override
    protected void clean() {
        Main.listener.versionNumber.decrementAndGet();
    }

    @Override
    protected void closeResources() {
        p.close();
        try {
            skt.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Main.listener.versionNumber.decrementAndGet();
    }

    @Override
    protected void doTask() throws IOException {
        PeerAddress my = new PeerAddress();
        my.setAddress(InetAddress.getByName("131.114.2.151"));
        my.setPort(BitConstants.PORT);
        my.setService(1);
        PeerAddress your = new PeerAddress();
        your.setAddress(p.getAddress());
        your.setPort(p.getPort());
        your.setService(p.getService());
        Version v = new Version();
        v.setMyAddress(my);
        v.setYourAddress(your);
        v.setServices(0);
        v.setTimestamp(System.currentTimeMillis() / BitConstants.TIME);
        v.setNonce(random.nextLong());
        if(p.getVersion())
            v.setVersion(BitConstants.VERSION);
        else
            v.setVersion(60001);
        v.setUserAgent("/BitKer/");
        v.setHeight(BitConstants.LASTBLOCK);
        v.setRelay(true);
        p.setPeerState(PeerState.HANDSHAKE);
        try {
            Connect.sendVersion(v, skt, p);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
