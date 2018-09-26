package com.bitker.api;

import com.bitker.Main;
import com.bitker.api.RequestHandler.*;
import com.bitker.api.RequestHandler.List;
import com.bitker.eventservice.EventService;
import com.bitker.eventservice.events.*;
import com.bitker.eventservice.filters.Filter;
import com.bitker.eventservice.filters.MsgFilter;
import com.bitker.eventservice.filters.PeerFilter;
import com.bitker.eventservice.filters.StateFilter;
import com.bitker.eventservice.subscribers.*;
import com.bitker.network.Peer;
import com.bitker.network.PeerState;
import com.bitker.protocol.Connect;
import com.bitker.protocol.ProtocolUtil;


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by machiara on 06/03/17.
 */
public class PublicInterfaceReader implements Runnable {

	private static final Handler unknown = new Unknown();
	private static final Handler [] handlers =
			new Handler[] {
					new Listen(),
					new ListenFrom(),
					new SendTo(),
					new SendToAll(),
					new List(),
					new Terminate(),
					new Stat(),
					new PeerStateChange(),
					new Connection(),
					new ListenOut()
			};

    private ByteBuffer msg;
    private ApiClientData data;


    public PublicInterfaceReader(ByteBuffer msg, ApiClientData data) {
        this.msg = msg;
        this.data = data;
        this.msg.clear();
    }

    @Override
    public void run() {
        int type;
        long id = -1;
        try
        {
            type = msg.getInt();
            id = msg.getLong();
            data.addId(id);
            handlers[type-1].handle(msg,data,id);
        }catch (Exception e){
            e.printStackTrace();
            unknown.handle(msg,data,id);
        }
    }



}
