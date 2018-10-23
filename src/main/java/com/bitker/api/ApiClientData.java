package com.bitker.api;

import com.bitker.Main;
import com.bitker.eventservice.EventService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by machiara on 03/03/17.
 */
public class ApiClientData {

    private int length;
    private ByteBuffer msgLen;
	private ByteBuffer magic;
    private ByteBuffer msg;
    private SocketChannel skt;
    private ConcurrentLinkedQueue<ByteBuffer []> queue;
    private Set<Long> ids;
    private SelectionKey key;
    private long lastTimeSeen;
    private boolean magicpassed;

    ApiClientData(SocketChannel skt){
        length = -1;
        msgLen = ByteBuffer.allocate(4);
        magic = ByteBuffer.allocate(4);
        queue = new ConcurrentLinkedQueue<>();
        this.skt = skt;
        ids = new HashSet<>();
        lastTimeSeen = System.currentTimeMillis();
        magicpassed = false;
    }

    void read(){
        lastTimeSeen = System.currentTimeMillis();
        try
        {
        	if(!magicpassed)
			{
				if(skt.read(magic) == -1)
					close();
				if(magic.position() != magic.limit())
					return;
				magic.clear();
				int val = magic.getInt();
				if(val != 420024)
				{

					System.out.println("API magic number error");
					System.out.println(val);
					close();
					return;
				}
				magicpassed = true;
				magic.clear();
			}
            if (length == -1)
            {

                if (skt.read(msgLen) == -1)
                    close();
                if (!(msgLen.position() == msgLen.limit()))
                    return;
                msgLen.clear();
                length = msgLen.getInt();
                System.out.println("API length "+length);
                msgLen.clear();
                msg = ByteBuffer.allocate(length);
            }
            if (skt.read(msg) == -1)
                close();
            if (!(msg.position() == msg.limit()))
                return;
            Main.publicInterface.ex.execute(new PublicInterfaceReader(msg, this));
            msg = null;
            magicpassed = false;
            length = -1;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            close();
        }
    }

    public void close() {
        try
        {
            skt.close();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
        key.cancel();
        Main.publicInterface.selector.wakeup();
        msg = null;
        queue = null;
        for(Long id : ids)
        {

            EventService.getInstance().unsubscribe(this,id);
        }
    }

    void write() {
        lastTimeSeen = System.currentTimeMillis();
        ByteBuffer [] msg = queue.peek();
        if(msg == null)
        {
            Main.publicInterface.registerChannel(skt,SelectionKey.OP_READ,this);
            return;
        }
        try
        {
            skt.write(msg);
            if(msg[msg.length - 1].position() == msg[msg.length - 1].capacity())
			{
				Main.sentMessage.incrementAndGet();
				queue.poll();
			}
            if(queue.isEmpty())
                Main.publicInterface.registerChannel(skt,SelectionKey.OP_READ, this);
        } catch (IOException e)
        {
            e.printStackTrace();
            close();
        }
    }

    public void addMsg(ByteBuffer... param) {
        for(ByteBuffer b : param)
            b.clear();
        queue.add(param);
        Main.publicInterface.registerChannel(skt, SelectionKey.OP_READ | SelectionKey.OP_WRITE,this);

    }

    synchronized void addId(long id) {
        ids.add(id);
    }

    void setKey(SelectionKey key) {
        this.key = key;
    }

    public void checkTimeout() {
        if(!queue.isEmpty() && System.currentTimeMillis() - lastTimeSeen > 30000)
            close();
    }
}
