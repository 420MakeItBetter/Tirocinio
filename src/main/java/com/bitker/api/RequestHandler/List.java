package com.bitker.api.RequestHandler;

import com.bitker.Main;
import com.bitker.api.ApiClientData;
import com.bitker.network.Peer;
import com.bitker.network.PeerState;
import com.bitker.utils.BitConstants;
import com.bitker.utils.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class List extends Handler {


    @Override
    public void handle(ByteBuffer msg, ApiClientData data, long id) {
        try {
            Set<String> peers = null;
            if (msg.hasRemaining())
            {
                int number = msg.getInt();
                peers = new HashSet<>(number);
                for (int i = 0; i < number; i++)
                {
                    byte[] addr = new byte[16];
                    peers.add(InetAddress.getByAddress(addr).getHostAddress());
                }
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            int size;
            if(peers != null)
                size = someList(peers,stream);
            else
                size = allList(stream);
            ByteBuffer message = ByteBuffer.wrap(stream.toByteArray());
            ByteBuffer tmp = ByteBuffer.allocate(4+4+8+4);
            tmp.putInt(4+8+4+message.limit());
            tmp.putInt(4);
            tmp.putLong(id);
            tmp.putInt(size);
            data.addMsg(ack(0,id));
            data.addMsg(tmp,message);
        } catch (IOException e)
        {
            data.addMsg(ack(2,id));
        }
    }

    private int allList(ByteArrayOutputStream str) throws IOException {
        DataOutputStream stream = new DataOutputStream(str);
        int size = 0;
        for(Peer p : Main.peers.values())
            if(p.getState() == PeerState.OPEN)
            {
                writePeer(p, stream);
                size++;
            }

        return size;
    }

    private int someList(Set<String> set, ByteArrayOutputStream str) throws IOException {
        DataOutputStream stream = new DataOutputStream(str);
        int size = 0;
        for(String s : set)
        {
            Peer p = Main.peers.get(s);
            if(p != null)
            {
                writePeer(p, stream);
                size++;
            }
        }

        return size;
    }

    private void writePeer(Peer p,DataOutputStream stream) throws IOException {
        byte [] addr = IOUtils.addressToByte(p.getAddress());
        stream.write(addr);
        stream.writeByte((int) p.getService());
        stream.writeByte(p.isIn() ? (byte) 1 : (byte) 0);
        stream.writeInt(p.getAgent().length());
        stream.write(p.getAgent().getBytes());
        stream.writeInt(p.theirVersion());
        stream.writeInt(p.getVersion() ? BitConstants.VERSION : 60001);
    }

}
