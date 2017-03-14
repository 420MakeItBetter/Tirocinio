package Client.eventservice.subscribers;

import Client.Protocol.ProtocolUtil;
import Client.eventservice.events.Event;
import Client.eventservice.events.MessageReceivedEvent;
import Client.utils.IOUtils;

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
                        msg.put(e.p.getAddress().getAddress());
                        data.addMsg(msg,header,payload);
                        break;
                    case 1:
                        msg.putInt(header.limit()+4+8+16);
                        msg.putInt(2);
                        msg.putLong(id);
                        msg.put(e.p.getAddress().getAddress());
                        data.addMsg(msg,header);
                        break;
                    case 2:
                        msg.putInt(payload.limit()+4+8+16);
                        msg.putInt(2);
                        msg.putLong(id);
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
