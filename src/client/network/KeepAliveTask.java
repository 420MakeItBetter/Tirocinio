package client.network;

import client.BitConstants;
import client.Main;
import client.protocol.KeepAlive;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;

/**
 * Created by Matteo on 15/11/2016.
 */
public class KeepAliveTask implements Runnable {

    @Override
    public void run() {
        long refresh = System.currentTimeMillis();
        while(!Main.terminate.get())
        {
            try
            {
                Thread.sleep(1000*60*2);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            System.out.println("KeepAlive Doing his job");
            if(System.currentTimeMillis() - refresh > 1000*60*24)
            {
                for(String s : BitConstants.DNS)
                {
                    try
                    {
                        InetAddress[] adresses = InetAddress.getAllByName(s);
                        for(InetAddress addr : adresses)
                        {
                            if(!Main.peers.containsKey(addr.getHostAddress()))
                            {
                                Peer p = new Peer(addr,BitConstants.PORT);
                                p.setTimestamp((int)(System.currentTimeMillis() / 1000));
                                Main.peers.put(addr.getHostAddress(),p);
                            }
                            else
                            {
                                Peer p = Main.peers.get(addr.getHostAddress());
                                p.resetAttempt();
                                p.setTimestamp((int) (System.currentTimeMillis() / 1000));
                            }
                        }
                    } catch (UnknownHostException e)
                    {
                        e.printStackTrace();
                    }
                    refresh = System.currentTimeMillis();
                }
            }
            for(Peer p : Main.peers.values())
            {
                if(p.getState() == PeerState.HANDSHAKE)
                {
                    if(System.currentTimeMillis() - p.getTime() > 1000*60*5)
                    {
                        if(p.getVersion())
                        {
                            p.close();
                            p.setVersion(false);
                            p.setPeerState(PeerState.CLOSE);
                            ConnectTask t = new ConnectTask(p);
                            Main.listener.ex.execute(t);
                        }
                        else
                            p.close();
                    }
                }
                if(p.getState() == PeerState.OPEN)
                {
                    if(System.currentTimeMillis() - p.getTime() > 1000*60)
                    {
                        try
                        {
                            KeepAlive.sendPing(p.getSocket(),p);
                        } catch (InterruptedException | ClosedChannelException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                if(p.getPeerState() == PeerState.CLOSE && System.currentTimeMillis() - p.getTime() > 1000*60*60*24)
                    Main.peers.remove(p.getAddress().getHostAddress());
            }

        }
    }
}
