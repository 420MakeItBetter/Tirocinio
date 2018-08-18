package com.bitker.eventservice.subscribers;

import com.bitker.eventservice.EventService;
import com.bitker.eventservice.events.Event;
import com.bitker.eventservice.events.NotConnectedEvent;
import com.bitker.eventservice.events.ConnectedEvent;
import com.bitker.protocol.ProtocolUtil;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;

/**
 * Created by machiara on 07/03/17.
 */
public class ConnectSubscriber extends Subscriber {

    public ByteBuffer header;
    public ByteBuffer payload;

    @Override
    public void inform(Event event) {
        ByteBuffer msg = ByteBuffer.allocate(4+4+8+16+1);
        msg.putInt(4+8+16+1);
        msg.putInt(6);
        msg.putLong(id);
        if(event instanceof ConnectedEvent)
        {
            if(((ConnectedEvent) event).p.getAddress().getAddress().length == 4)
                msg.put(new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                        (byte)0x00,(byte)0xFF,(byte)0xFF});
            msg.put(((ConnectedEvent) event).p.getAddress().getAddress());
            msg.put((byte) 1);
            if(header != null)
                try {
                    ProtocolUtil.sendMessage(header,payload,((ConnectedEvent) event).p.getSocket(),((ConnectedEvent) event).p,id);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
        }
        if(event instanceof NotConnectedEvent)
        {
            if(((NotConnectedEvent) event).p.getAddress().getAddress().length == 4)
                msg.put(new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                        (byte)0x00,(byte)0xFF,(byte)0xFF});
            msg.put(((NotConnectedEvent) event).p.getAddress().getAddress());
            msg.put((byte) 0);
            EventService.getInstance().unsubscribe(data,id);
            this.header = null;
            this.payload = null;
        }
        data.addMsg(msg);
    }
}
