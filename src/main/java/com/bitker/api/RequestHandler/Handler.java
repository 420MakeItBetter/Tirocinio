package com.bitker.api.RequestHandler;

import com.bitker.api.ApiClientData;

import java.nio.ByteBuffer;

public abstract class Handler {

    public abstract void handle(ByteBuffer msg, ApiClientData data, long id);

    protected ByteBuffer ack(int cause, long id){
        ByteBuffer msg = id == -1 ? ByteBuffer.allocate(4+4+4) : ByteBuffer.allocate(4+4+4+8);
        msg.putInt(msg.limit()-4);
        msg.putInt(1);
        msg.putInt(cause);
        if(id != -1)
            msg.putLong(id);
        return msg;
    }

}
