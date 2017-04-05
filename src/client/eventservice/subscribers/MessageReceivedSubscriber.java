package client.eventservice.subscribers;

import client.protocol.ProtocolUtil;
import client.eventservice.events.Event;
import client.eventservice.events.MessageReceivedEvent;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by machiara on 06/03/17.
 */
public class MessageReceivedSubscriber extends Subscriber {

    public byte what;

    @Override
    public void inform(Event event) {
        try
        {

            if (event instanceof MessageReceivedEvent)
            {
                MessageReceivedEvent e = (MessageReceivedEvent) event;
                ByteBuffer header = ProtocolUtil.writeHeader(e.m);
                ByteBuffer payload = ProtocolUtil.writePayload(e.m);
                header.put(ProtocolUtil.getChecksum(payload));
                ByteBuffer msg = ByteBuffer.allocate(4+4+8+16);
                switch (what)
                {
                    case 0:
                        msg.putInt(header.limit()+payload.limit()+4+8+16);
                        msg.putInt(2);
                        msg.putLong(id);
                        if(e.p.getAddress().getAddress().length == 4)
                            msg.put(new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                                    (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                                    (byte)0x00,(byte)0xFF,(byte)0xFF});
                        msg.put(e.p.getAddress().getAddress());
                        data.addMsg(msg,header,payload);
                        break;
                    case 1:
                        msg.putInt(header.limit()+4+8+16);
                        msg.putInt(2);
                        msg.putLong(id);
                        if(e.p.getAddress().getAddress().length == 4)
                            msg.put(new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                                    (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                                    (byte)0x00,(byte)0xFF,(byte)0xFF});
                        msg.put(e.p.getAddress().getAddress());
                        data.addMsg(msg,header);
                        break;
                    case 2:
                        msg.putInt(payload.limit()+4+8+16);
                        msg.putInt(2);
                        msg.putLong(id);
                        if(e.p.getAddress().getAddress().length == 4)
                            msg.put(new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                                    (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                                    (byte)0x00,(byte)0xFF,(byte)0xFF});
                        msg.put(e.p.getAddress().getAddress());
                        data.addMsg(msg,payload);
                }

            }
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
