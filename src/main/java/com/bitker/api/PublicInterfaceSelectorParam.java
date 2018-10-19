package com.bitker.api;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;

class PublicInterfaceSelectorParam {

	private AbstractSelectableChannel channel;
	private int interest;
	private ApiClientData data;

	PublicInterfaceSelectorParam(AbstractSelectableChannel c, int i, ApiClientData d){
		channel = c;
		interest = i;
		data = d;
	}

	SelectionKey register(Selector selector) throws ClosedChannelException {
		SelectionKey k = channel.register(selector,interest,data);
		data.setKey(k);
		return k;
	}

	ApiClientData getAttachment(){
		return data;
	}
}
