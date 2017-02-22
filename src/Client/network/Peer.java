package Client.network;



import Client.messages.SerializedMessage;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Created by Matteo on 11/10/2016.
 *
 */
public class Peer implements Comparable<Peer>{

    private InetAddress addr;
    private int attempt;
    private int port;
    private int timestamp;
    private long service;
    private boolean newVersion;
    private long lastMessage;
    private ConcurrentLinkedQueue<SerializedMessage> pendingMessages;
    private SerializedMessage incompleteMsg;
    private PeerState state;
    private SocketChannel skt;
    private String agent;

    public Peer(InetAddress addr,int port){
        attempt = 0;
        newVersion = true;
        pendingMessages = new ConcurrentLinkedQueue<>();
        this.addr = addr;
        this.port = port;
        state = PeerState.CLOSE;
        skt = null;
        incompleteMsg = null;
    }

    public void setAgent(String s){
        agent = s;
    }

    public String getAgent(){
        return agent;
    }

    public void setTime(){
        lastMessage = System.currentTimeMillis();
    }

    public long getTime(){
        return lastMessage;
    }

    public void setSocket(SocketChannel skt){
        this.skt = skt;
    }

    public SocketChannel getSocket(){
        return skt;
    }

    public SerializedMessage getMsg() {
        return incompleteMsg;
    }


    public void setMsg(SerializedMessage msg) {
        this.incompleteMsg = msg;
    }


    public PeerState getState() {
        return state;
    }

    public void setPeerState(PeerState state) {
        this.state = state;
    }


    public InetAddress getAddress() {
        return addr;
    }

    public int getPort() {
        return port;
    }

    public  synchronized void addMsg(SerializedMessage message) {
        pendingMessages.add(message);
    }

    public SerializedMessage peekMsg() {
        return pendingMessages.peek();
    }

    public SerializedMessage poolMsg() {
        return pendingMessages.poll();
    }

    public boolean hasNoPendingMessage() {
        return pendingMessages.isEmpty();
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getTimestamp() {
        return timestamp;
    }


    public void setService(long service) {
        this.service = service;
    }

    public long getService() {
        return service;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public PeerState getPeerState() {
        return state;
    }

    @Override
    public int compareTo(Peer o) {
        if(this.getAddress().getHostAddress().equals("176.10.116.242"))
            return 1;
        else if(o.getAddress().equals("176.10.116.242"))
            return -1;
        int res = this.attempt - o.attempt;
        if(res == 0)
            res = o.timestamp - this.timestamp;
        return res;
    }

    @Override
    public boolean equals(Object obj) {
        Peer o = (Peer) obj;
        if(addr.getHostAddress().equals(o.getAddress().getHostAddress()))
            if(port == o.getPort())
                return true;
        return false;
    }

    public void incrementAttempt() {
        attempt++;
    }

    public int getAttempt(){
        return attempt;
    }

    @Override
    public String toString() {
        return addr.toString()+" "+port+" "+service+" "+state+"\n"+incompleteMsg+"\n"+pendingMessages;
    }


    public boolean getVersion() {
        return newVersion;
    }

    public void setVersion(boolean version) {
        this.newVersion = version;
    }

    public void close(){
        this.state = PeerState.CLOSE;
        for(SerializedMessage msg : pendingMessages)
        {
            try
            {
                SerializedMessage.returnHeader(msg.getHeader());
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            try
            {
                SerializedMessage.returnPayload(msg.getPayload());
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        if(incompleteMsg != null)
        {
            try
            {
                SerializedMessage.returnHeader(incompleteMsg.getHeader());
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            try
            {
                SerializedMessage.returnPayload(incompleteMsg.getPayload());
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            if(this.skt != null)
                this.skt.close();
            this.skt = null;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        newVersion = true;
        attempt++;
    }

}

