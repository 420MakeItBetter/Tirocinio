package Client.commands;

import Client.Main;
import Client.network.Peer;
import Client.network.PeerState;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Matteo on 15/11/2016.
 */
public class Update extends Command {

    @Override
    public void execute(ObjectOutputStream out) {
        List<String> peers = new LinkedList<>();

        for(Peer p : Main.peers.values())
            if(p.getState() == PeerState.OPEN)
                peers.add(p.getAddress().getHostAddress()+"/"+p.getAgent());
        try
        {
            out.writeUnshared(peers);
        } catch (IOException ignored)
        {}

    }
}
