package com.bitker.api;


import com.bitker.api.RequestHandler.*;
import com.bitker.api.RequestHandler.List;


import java.nio.ByteBuffer;

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


    PublicInterfaceReader(ByteBuffer msg, ApiClientData data) {
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
            System.out.println("type: "+type+"id "+id);
            data.addId(id);
            System.out.println("API RECEIVED "+type+" id "+id);
            handlers[type-1].handle(msg,data,id);
        }catch (Exception e){
            e.printStackTrace();
            unknown.handle(msg,data,id);
        }
    }



}
