package com.bitker.eventservice.subscribers;

import com.bitker.eventservice.events.Event;
import com.bitker.eventservice.events.MessageSentEvent;

import java.nio.ByteBuffer;

/**
 * Created by matteo on 21/04/17.
 */
public class NewMessageSentSubscriber extends Subscriber {
    @Override
    public void inform(Event event) {
        if(event instanceof MessageSentEvent)
        {
            ByteBuffer msg = ByteBuffer.allocate(4+4+8+16+12);
            msg.putInt(4+8+16+12);
            msg.putInt(8);
            msg.putLong(id);
            MessageSentEvent e = (MessageSentEvent) event;
            if(e.p.getAddress().getAddress().length == 4)
                msg.put(new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                        (byte)0x00,(byte)0xFF,(byte)0xFF});
            msg.put(e.p.getAddress().getAddress());
            byte [] type = new byte[12];
            int i = 0;
            for(char c : e.msg.getCommand().toCharArray())
            {
                type[i] = (byte) c;
                i++;
            }
            msg.put(type);
            data.addMsg(msg);
        }
    }
}
