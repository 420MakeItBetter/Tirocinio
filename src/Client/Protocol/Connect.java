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


    public static final AtomicInteger connections = new AtomicInteger();

    public static void connect(InetAddress address,Peer p) throws IOException {
        SocketChannel skt = SocketChannel.open();
        try
        {
            skt.configureBlocking(false);
            skt.connect(new InetSocketAddress(address, BitConstants.PORT));
            Main.listener.addChannel(skt, SelectionKey.OP_CONNECT, p);
        }
        catch (IOException e)
        {
            skt.close();
            throw e;
        }
    }

    public static void connect(InetAddress address, int port, Peer p){
        ConnectTask t = new ConnectTask(p);
        Main.listener.ex.execute(t);
    }

    public static void sendVersion(Version msg, SocketChannel channel, Peer p) throws InterruptedException, ClosedChannelException {
        ByteBuffer header = ProtocolUtil.writeHeader(msg);
        ByteBuffer[] payload = new ByteBuffer[0];
        try
        {
            payload = ProtocolUtil.writePayload(msg);
        } catch (IOException e)
        {
            SerializedMessage.returnHeader(header);
        }
        header.put(ProtocolUtil.getChecksum(payload));

        ProtocolUtil.sendMessage(header,payload,channel,p);
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
        int count = 0;
        for(Peer peer : Main.peers.values())
        {
            count++;
            if(count == 1000)
                break;
            PeerAddress pa = new PeerAddress();
            pa.setAddress(peer.getAddress());
            pa.setPort(peer.getPort());
            pa.setService(p.getService());
            pa.setTime(p.getTimestamp());
            addr.getAddresses().add(pa);
        }
        ByteBuffer header = ProtocolUtil.writeHeader(addr);
        ByteBuffer[] payload = new ByteBuffer[0];
        try
        {
            payload = ProtocolUtil.writePayload(addr);
        } catch (IOException e)
        {
            SerializedMessage.returnHeader(header);
        }
        header.put(ProtocolUtil.getChecksum(payload));

        ProtocolUtil.sendMessage(header,payload,skt,p);

    }

    public static void sendGetAddress(SocketChannel skt, Peer p) throws ClosedChannelException, InterruptedException {
        GetAddress ga = new GetAddress();
        ByteBuffer message = ProtocolUtil.writeHeader(ga);
        message.put(IOUtils.intToByteArray(BitConstants.CHECKSUM));

        ProtocolUtil.sendMessage(message,null,skt,p);
    }


}
