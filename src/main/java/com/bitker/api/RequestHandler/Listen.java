package com.bitker.api.RequestHandler;

import com.bitker.api.ApiClientData;
import com.bitker.eventservice.EventService;
import com.bitker.eventservice.events.MessageReceivedEvent;
import com.bitker.eventservice.filters.MsgFilter;
import com.bitker.eventservice.subscribers.MessageReceivedSubscriber;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class Listen extends Handler {

    @Override
    public void handle(ByteBuffer msg, ApiClientData data, long id) {
        MessageReceivedSubscriber sub = new MessageReceivedSubscriber();
        sub.id = id;
        sub.data = data;
        int n = msg.getInt();
        Set<String> msgTypes = new HashSet<>(n);
        for(int i = 0; i < n; i++)
        {
            byte [] bytes = new byte [12];
            msg.get(bytes);
            String s = new String(bytes).trim();
            msgTypes.add(s);
        }
        byte b = msg.get();
        sub.what = b;
        MsgFilter filter = new MsgFilter(msgTypes);
        EventService.getInstance().subscribe(MessageReceivedEvent.class,filter,sub);
        data.addMsg(ack(0,id));
    }
}
