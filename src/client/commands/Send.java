package client.commands;

import client.BitConstants;
import client.Main;
import client.protocol.Connect;
import client.protocol.ProtocolUtil;
import client.messages.Message;
import client.network.Peer;
import client.network.PeerState;
import client.utils.IOUtils;

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
                ByteBuffer header = ProtocolUtil.writeHeader(m);
                ByteBuffer payload = null;
                if(m.getLength() > 0)
                    try
                    {
                        payload = ProtocolUtil.writePayload(m);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                if(m.getLength() > 0)
                    header.put(ProtocolUtil.getChecksum(payload));
                else
                {
                    header.put(IOUtils.intToByteArray(BitConstants.CHECKSUM));
                    header.rewind();
                }
                try
                {
                    ProtocolUtil.sendMessage(header, payload, p.getSocket(), p);
                } catch (ClosedChannelException e)
                {
                }
            }
            else
            {
                Connect.connect(p.getAddress(),p.getPort(),p);
            }
        }

    }
}
