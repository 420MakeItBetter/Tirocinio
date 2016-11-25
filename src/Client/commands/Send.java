package Client.commands;

import Client.Main;
import Client.Protocol.ProtocolUtil;
import Client.messages.Message;
import Client.messages.SerializedMessage;
import Client.network.Peer;
import Client.network.PeerState;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.List;

/**
 * Created by Matteo on 15/11/2016.
 */
public class Send extends Command {

    private Message m;
    private List<InetAddress> addr;

    public Send(Message m,List<InetAddress> addr){
        this.m = m;
        this.addr = addr;
    }

    @Override
    public void execute(ObjectOutputStream out) {
        for(InetAddress addr : this.addr)
        {
            Peer p = Main.peers.get(addr.getHostAddress());
            if (p.getState() == PeerState.OPEN)
            {
                ByteBuffer header = null;
                try
                {
                    header = ProtocolUtil.writeHeader(m);
                } catch (InterruptedException ignored)
                {
                }
                ByteBuffer[] payload = null;
                try
                {
                    payload = ProtocolUtil.writePayload(m);
                } catch (InterruptedException ignored)
                {
                } catch (IOException e)
                {
                    try
                    {
                        SerializedMessage.returnHeader(header);
                    } catch (InterruptedException ignored)
                    {
                    }
                    return;
                }
                header.put(ProtocolUtil.getChecksum(payload));
                try
                {
                    ProtocolUtil.sendMessage(header, payload, p.getSocket(), p);
                } catch (ClosedChannelException e)
                {
                    return;
                }
            }
            try
            {
                out.writeUnshared(new NullResponse());
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }
}