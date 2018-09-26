package com.bitker.api.RequestHandler;

import com.bitker.Main;
import com.bitker.api.ApiClientData;
import com.bitker.eventservice.EventService;
import com.bitker.eventservice.events.*;
import com.bitker.eventservice.filters.PeerFilter;
import com.bitker.eventservice.filters.StateFilter;
import com.bitker.eventservice.subscribers.ConnectSubscriber;
import com.bitker.eventservice.subscribers.MessageSentSubscriber;
import com.bitker.eventservice.subscribers.PeerStateOpenedSubscriber;
import com.bitker.eventservice.subscribers.PeerStateSubscriber;
import com.bitker.network.Peer;
import com.bitker.network.PeerState;
import com.bitker.protocol.Connect;
import com.bitker.protocol.ProtocolUtil;
import com.bitker.utils.IOUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.HashSet;

public class SendTo extends Handler {

    @Override
    public void handle(ByteBuffer msg, ApiClientData data, long id) {
        MessageSentSubscriber sub = new MessageSentSubscriber();
        sub.id = id;
        sub.data = data;
        byte [] arr = new byte [16];
        msg.get(arr);
        try {
            String s = InetAddress.getByAddress(arr).getHostAddress();
            HashSet<String> set = new HashSet<>(1);
            set.add(s);
            PeerFilter filter = new PeerFilter(set);
            EventService.getInstance().subscribe(MessageSentEvent.class,filter,sub);
            data.addMsg(ack(0,id));
            PeerStateSubscriber psub = new PeerStateSubscriber();
            psub.id = id;
            psub.data = data;
            EventService.getInstance().subscribe(PeerStateChangedEvent.class,filter,psub);
            ByteBuffer message = ByteBuffer.allocate(4+4+8+16+1);
            message.putInt(4+8+16+1);
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
                        ConnectSubscriber csub = new ConnectSubscriber();
                        csub.id = id;
                        csub.data = data;
                        ByteBuffer header = ByteBuffer.allocate(24);
                        while (header.hasRemaining())
                            header.put(msg.get());
                        ByteBuffer payload = ByteBuffer.allocate(msg.remaining());
                        payload.put(msg);
                        csub.header = header;
                        csub.payload = payload;
                        EventService.getInstance().subscribe(ConnectedEvent.class,filter,csub);
                        EventService.getInstance().subscribe(NotConnectedEvent.class,filter,csub);
                        Connect.connect(p.getAddress(),8333,p);
                        break;
                    case HANDSHAKE:
                        message.put((byte) 1);
                        PeerStateOpenedSubscriber posub = new PeerStateOpenedSubscriber();
                        posub.id = id;
                        posub.data = data;
                        header = ByteBuffer.allocate(24);
                        while (header.hasRemaining())
                            header.put(msg.get());
                        payload = ByteBuffer.allocate(msg.remaining());
                        payload.put(msg);
                        posub.header = header;
                        posub.payload = payload;
                        StateFilter sfilter = new StateFilter(PeerState.OPEN);
                        EventService.getInstance().subscribe(PeerStateChangedEvent.class,sfilter,posub);
                        break;
                    case OPEN:
                        message.put((byte) 2);
                        header = ByteBuffer.allocate(24);
                        while (header.hasRemaining())
                            header.put(msg.get());
                        payload = ByteBuffer.allocate(msg.remaining());
                        payload.put(msg);
                        ProtocolUtil.sendMessage(header,payload,p.getSocket(),p,id);

                }
            }
            else
            {
                Peer p = new Peer(InetAddress.getByName(s), 8333);
                Main.peers.put(s,p);
                ConnectSubscriber csub = new ConnectSubscriber();
                csub.id = id;
                csub.data = data;
                ByteBuffer header = ByteBuffer.allocate(24);
                while (header.hasRemaining())
                    header.put(msg.get());
                ByteBuffer payload = ByteBuffer.allocate(msg.remaining());
                payload.put(msg);
                csub.header = header;
                csub.payload = payload;
                EventService.getInstance().subscribe(ConnectedEvent.class,filter,csub);
                EventService.getInstance().subscribe(NotConnectedEvent.class,filter,csub);
                Connect.connect(p.getAddress(),8333,p);
                byte [] addr = IOUtils.addressToByte(p.getAddress());
                message.put(addr);
                message.put((byte) 0);
            }
            data.addMsg(message);
        } catch (UnknownHostException e) {
            data.addMsg(ack(2,id));
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }
}
