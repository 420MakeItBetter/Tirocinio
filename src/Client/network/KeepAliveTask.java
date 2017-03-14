package Client.network;

import Client.Main;
import Client.Protocol.KeepAlive;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

/**
 * Created by Matteo on 15/11/2016.
 */
public class KeepAliveTask implements Runnable {

    @Override
    public void run() {
        while(true)
        {
            try
            {
                Thread.sleep(1000*60*10);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            for(Peer p : Main.peers.values())
            {
                if(p.getState() == PeerState.HANDSHAKE)
                {
                    if(System.currentTimeMillis() - p.getTime() > 1000*60*5)
                    {
                        if(p.getVersion())
                        {
                            p.setVersion(false);
                            p.setPeerState(PeerState.CLOSE);
                            VersionTask t = new VersionTask(p.getSocket(),p);
                            Main.listener.ex.execute(t);
                        }
                        else
                        {
                            try
                            {
                                p.getSocket().close();
                                Main.openedFiles.decrementAndGet();
                            } catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                            if(p.close())
                                Main.oldnotConnectedAdressess.add(p);
                        }
                    }
                }
                if(p.getState() == PeerState.OPEN)
                {
                    if(System.currentTimeMillis() - p.getTime() > 1000*60*10)
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
            }

        }
    }
}
