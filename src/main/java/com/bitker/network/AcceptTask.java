package com.bitker.network;

import com.bitker.Main;
import com.bitker.eventservice.EventService;
import com.bitker.eventservice.events.ConnectedEvent;

import java.io.IOException;
import java.net.StandardSocketOptions;
import java.nio.channels.*;

/**
 * Created by Matteo on 11/10/2016.
 *
 */
public class AcceptTask implements Runnable{

    private SocketChannel skt;

    AcceptTask(SocketChannel skt) {
        Main.listener.acceptNumber.incrementAndGet();
        this.skt = skt;
    }


    @Override
    public void run() {
        try
        {
            skt.setOption(StandardSocketOptions.SO_REUSEADDR,true);
            skt.configureBlocking(false);
            Peer peer;
            if(Main.peers.containsKey(skt.socket().getInetAddress().getHostAddress()))
            {
                peer = Main.peers.get(skt.socket().getInetAddress().getHostAddress());
                if(peer.getState() != PeerState.CLOSE)
                {
                    skt.close();
                    Main.openedFiles.decrementAndGet();
                    return;
                }
            }
            else
            {
                peer = new Peer(skt.socket().getInetAddress(), 8333);
                Main.peers.put(peer.getAddress().getHostAddress(), peer);
            }
            peer.setIn(true);
            peer.setPeerState(PeerState.HANDSHAKE);
            peer.setSocket(skt);
            Main.listener.addChannel(skt, SelectionKey.OP_WRITE | SelectionKey.OP_READ,peer);
            VersionTask v = new VersionTask(skt,peer);
            Main.listener.ex.execute(v);

            EventService.getInstance().publish(new ConnectedEvent(peer));
        } catch (IOException e)
        {
            e.printStackTrace();
            try
            {
                skt.close();
                Main.openedFiles.decrementAndGet();
            } catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }
        finally
        {
            Main.listener.acceptNumber.decrementAndGet();
        }
    }
}
