package client.protocol;

import client.BitConstants;
import client.Main;
import client.messages.*;
import client.network.ConnectTask;
import client.network.Peer;
import client.utils.IOUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

/**
 * Created by Matteo on 17/10/2016.
 *
 */
public class Connect {



    public static void connect(InetAddress address, int port, Peer p){
        ConnectTask t = new ConnectTask(p);
        Main.listener.ex.execute(t);
    }

    public static void sendVersion(Version msg, SocketChannel channel, Peer p) throws InterruptedException, ClosedChannelException {
        ByteBuffer header = ProtocolUtil.writeHeader(msg);
        ByteBuffer payload = null;
        try
        {
            payload = ProtocolUtil.writePayload(msg);
            header.put(ProtocolUtil.getChecksum(payload));
            ProtocolUtil.sendMessage(header,payload,channel,p);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

    }


    public static void sendVerAck(VerAck msg,SocketChannel skt, Peer p) throws ClosedChannelException, InterruptedException {
        ByteBuffer message = ProtocolUtil.writeHeader(msg);
        message.position(BitConstants.CHECKSUMPOSITION);
        message.put(IOUtils.intToByteArray(BitConstants.CHECKSUM));
        message.rewind();

        ProtocolUtil.sendMessage(message,null,skt,p);
    }


    public static void sendAddresses(SocketChannel skt, Peer p) throws InterruptedException, ClosedChannelException {


        Address addr = new Address();
        for(Peer pp : Main.peers.values())
            if(!pp.equals(p))
            {
                PeerAddress pa = new PeerAddress();
                pa.setAddress(pp.getAddress());
                pa.setPort(pp.getPort());
                pa.setService(pp.getService());
                pa.setTime(pp.getTimestamp());
                addr.getAddresses().add(pa);
                break;
            }
        try
        {
            ByteBuffer header = ProtocolUtil.writeHeader(addr);
            ByteBuffer payload = ProtocolUtil.writePayload(addr);
            header.put(ProtocolUtil.getChecksum(payload));
            ProtocolUtil.sendMessage(header,payload,skt,p);
        } catch (IOException e)
        {
            e.printStackTrace();
        }





    }

    public static void sendGetAddress(SocketChannel skt, Peer p) throws ClosedChannelException, InterruptedException {
        GetAddress ga = new GetAddress();
        ByteBuffer message = ProtocolUtil.writeHeader(ga);
        message.put(IOUtils.intToByteArray(BitConstants.CHECKSUM));

        ProtocolUtil.sendMessage(message,null,skt,p);
    }


}
