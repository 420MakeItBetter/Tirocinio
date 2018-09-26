package com.bitker.api.RequestHandler;

import com.bitker.api.ApiClientData;
import com.bitker.eventservice.EventService;
import com.bitker.eventservice.events.ConnectedEvent;
import com.bitker.eventservice.subscribers.ConnectionSubscriber;

import java.nio.ByteBuffer;

public class Connection extends Handler {

	@Override
	public void handle(ByteBuffer msg, ApiClientData data, long id) {
		ConnectionSubscriber sub = new ConnectionSubscriber();
		sub.id = id;
		sub.data = data;
		EventService.getInstance().subscribe(ConnectedEvent.class,null,sub);
		data.addMsg(ack(0,id));
	}
}
