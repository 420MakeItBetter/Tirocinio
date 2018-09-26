package com.bitker.api.RequestHandler;

import com.bitker.api.ApiClientData;

import java.nio.ByteBuffer;

public class Unknown extends Handler {

	@Override
	public void handle(ByteBuffer msg, ApiClientData data, long id) {
		data.addMsg(ack(2,id));
	}
}
