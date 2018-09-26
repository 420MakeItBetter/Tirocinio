package com.bitker.api.RequestHandler;

import com.bitker.api.ApiClientData;
import com.bitker.eventservice.EventService;
import com.bitker.eventservice.events.PeerStateChangedEvent;
import com.bitker.eventservice.filters.StateFilter;
import com.bitker.eventservice.subscribers.PeerStateChangedSubscriber;
import com.bitker.network.PeerState;

import java.nio.ByteBuffer;

public class PeerStateChange extends Handler {

	@Override
	public void handle(ByteBuffer msg, ApiClientData data, long id) {
		PeerStateChangedSubscriber sub = new PeerStateChangedSubscriber();
		sub.id = id;
		sub.data = data;
		byte b = msg.get();
		StateFilter filter;
		switch (b)
		{
			case 1 :
				filter = new StateFilter(PeerState.OPEN);
				break;
			case 2 :
				filter = new StateFilter(PeerState.CLOSE);
				break;
			case 3 :
				filter = new StateFilter(PeerState.HANDSHAKE);
				break;
			default :
				filter = null;
		}
		EventService.getInstance().subscribe(PeerStateChangedEvent.class,filter,sub);
		data.addMsg(ack(0,id));
	}
}
