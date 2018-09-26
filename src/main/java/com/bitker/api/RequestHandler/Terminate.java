package com.bitker.api.RequestHandler;

import com.bitker.api.ApiClientData;
import com.bitker.eventservice.EventService;

import java.nio.ByteBuffer;

public class Terminate extends Handler {

	@Override
	public void handle(ByteBuffer msg, ApiClientData data, long id) {
		long toTerminate = msg.getLong();
		EventService.getInstance().unsubscribe(data,toTerminate);
		data.addMsg(ack(0,id));
	}
}
