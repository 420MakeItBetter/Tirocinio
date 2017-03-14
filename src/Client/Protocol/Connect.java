package Client.Protocol;

import Client.BitConstants;
import Client.Main;
import Client.bitio.LittleEndianOutputStream;
import Client.messages.*;
import Client.network.ConnectTask;
import Client.network.Peer;
import Client.utils.IOUtils;
import io.nayuki.bitcoin.crypto.Sha256;
import io.nayuki.bitcoin.crypto.Sha256Hash;

import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketOptions;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Matteo on 17/10/2016.
 *
 */
public class Connect {



    public static void connect(InetAddress address, int port, Peer p){
        ConnectTask t = new ConnectTask(p,true);
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
        int i = 0;
        for(Peer pp : Main.peers.values())
        {
            if(i == 1000)
                break;
            PeerAddress pa = new PeerAddress();
            pa.setTime(pp.getTimestamp());
            pa.setService(pp.getService());
            pa.setPort(pp.getPort());
            pa.setAddress(pp.getAddress());
            addr.getAddresses().add(pa);
            i++;
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
