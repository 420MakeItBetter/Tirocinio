package com.bitker.api.RequestHandler;

import com.bitker.Main;
import com.bitker.api.ApiClientData;
import com.bitker.eventservice.EventService;
import com.bitker.eventservice.events.MessageSentEvent;
import com.bitker.eventservice.events.PeerStateChangedEvent;
import com.bitker.eventservice.filters.MsgFilter;
import com.bitker.eventservice.filters.PeerFilter;
import com.bitker.eventservice.subscribers.ChangePeerSubscriber;
import com.bitker.eventservice.subscribers.MessageSentToAllSubscriber;
import com.bitker.network.Peer;
import com.bitker.network.PeerState;
import com.bitker.protocol.ProtocolUtil;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.HashSet;
import java.util.Set;

public class SendToAll extends Handler {


    @Override
    public void handle(ByteBuffer msg, ApiClientData data, long id) {
        MessageSentToAllSubscriber sub = new MessageSentToAllSubscriber();
        sub.id = id;
        sub.data = data;
        int number = msg.getInt();
        sub.n = number;
        int pos = msg.position();
        Set<String> peers = new HashSet<>(number);
        MsgFilter filter = new MsgFilter(id);
        EventService.getInstance().subscribe(MessageSentEvent.class,filter,sub);
        try {
            for (Peer p : Main.peers.values()) {
                if (number < 0)
                    break;
                if (p.getState() == PeerState.OPEN) {
                    ByteBuffer header = ByteBuffer.allocate(24);
                    while (header.hasRemaining())
                        header.put(msg.get());
                    ByteBuffer payload = ByteBuffer.allocate(msg.remaining());
                    payload.put(msg);
                    msg.position(pos);
                    ProtocolUtil.sendMessage(header, payload, p.getSocket(), p, id);
                    peers.add(p.getAddress().getHostAddress());
                    number--;
                }
            }
            ChangePeerSubscriber cpsub = new ChangePeerSubscriber();
            cpsub.id = -1;
            cpsub.data = data;
            ByteBuffer header = ByteBuffer.allocate(24);
            while (header.hasRemaining())
                header.put(msg.get());
            ByteBuffer payload = ByteBuffer.allocate(msg.remaining());
            payload.put(msg);
            PeerFilter pfilter = new PeerFilter(peers);
            cpsub.peers = peers;
            cpsub.header = header;
            cpsub.payload = payload;
            cpsub.reqid = id;
            EventService.getInstance().subscribe(PeerStateChangedEvent.class,pfilter,cpsub);
            data.addMsg(ack(0,id));
        } catch (ClosedChannelException e) {
            data.addMsg(ack(2,id));
        }
    }
}
