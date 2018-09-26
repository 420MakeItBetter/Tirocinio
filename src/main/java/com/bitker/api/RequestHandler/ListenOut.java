package com.bitker.api.RequestHandler;

import com.bitker.api.ApiClientData;
import com.bitker.eventservice.EventService;
import com.bitker.eventservice.events.MessageSentEvent;
import com.bitker.eventservice.subscribers.NewMessageSentSubscriber;

import java.nio.ByteBuffer;

public class ListenOut extends Handler {

	@Override
	public void handle(ByteBuffer msg, ApiClientData data, long id) {
		NewMessageSentSubscriber sub = new NewMessageSentSubscriber();
		sub.id = id;
		sub.data = data;
		EventService.getInstance().subscribe(MessageSentEvent.class,null,sub);
		data.addMsg(ack(0,id));
	}
}
