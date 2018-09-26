package com.bitker.api.RequestHandler;

import com.bitker.Main;
import com.bitker.api.ApiClientData;
import com.bitker.eventservice.EventService;
import com.bitker.eventservice.events.ConnectedEvent;
import com.bitker.eventservice.events.MessageReceivedEvent;
import com.bitker.eventservice.events.NotConnectedEvent;
import com.bitker.eventservice.events.PeerStateChangedEvent;
import com.bitker.eventservice.filters.PeerFilter;
import com.bitker.eventservice.subscribers.ConnectSubscriber;
import com.bitker.eventservice.subscribers.MessageReceivedSubscriber;
import com.bitker.eventservice.subscribers.PeerStateSubscriber;
import com.bitker.network.Peer;
import com.bitker.protocol.Connect;
import com.bitker.utils.IOUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class ListenFrom extends Handler {
    @Override
    public void handle(ByteBuffer msg, ApiClientData data, long id) {
        MessageReceivedSubscriber sub = new MessageReceivedSubscriber();
        sub.id = id;
        sub.data = data;
        int n = msg.getInt();
        Set<String> peers = new HashSet<>(n);
        try {
            for (int i = 0; i < n; i++) {
                byte[] bytes = new byte[16];
                msg.get(bytes);
                String s = InetAddress.getByAddress(bytes).getHostAddress();
                peers.add(s);
            }
            byte b = msg.get();
            sub.what = b;
            PeerFilter filter = new PeerFilter(peers);
            EventService.getInstance().subscribe(MessageReceivedEvent.class,filter,sub);
            PeerStateSubscriber psub = new PeerStateSubscriber();
            psub.id = id;
            psub.data = data;
            EventService.getInstance().subscribe(PeerStateChangedEvent.class,filter,psub);
            data.addMsg(ack(0, id));
            for(String s : peers)
            {
                ByteBuffer message = ByteBuffer.allocate(4+4+8+16+1);
                message.putInt(4+9+16+1);
                message.putInt(9);
                message.putLong(id);

                if(Main.peers.containsKey(s))
                {
                    Peer p = Main.peers.get(s);
                    byte [] addr = IOUtils.addressToByte(p.getAddress());
                    message.put(addr);
                    switch (p.getState())
                    {
                        case CLOSE:
                            message.put((byte) 0);
                            Connect.connect(p.getAddress(),833,p);
                            break;
                        case HANDSHAKE:
                            message.put((byte) 1);
                            break;
                        case OPEN:
                            message.put((byte) 2);
                            break;
                    }
                }
                else
                {
                    Peer p = new Peer(InetAddress.getByName(s),8333);
                    Main.peers.put(s,p);
                    Connect.connect(p.getAddress(),8333,p);
                    byte [] addr = IOUtils.addressToByte(p.getAddress());
                    message.put(addr);
                    message.put((byte) 0);
                }
                data.addMsg(message);
            }
            ConnectSubscriber csub = new ConnectSubscriber();
            csub.id = id;
            csub.data = data;
            EventService.getInstance().subscribe(ConnectedEvent.class,filter,csub);
            EventService.getInstance().subscribe(NotConnectedEvent.class,filter,csub);

        } catch (UnknownHostException e) {
            data.addMsg(ack(2,id));
        }
    }
}
