package com.bitker.network;



import com.bitker.Main;
import com.bitker.eventservice.EventService;
import com.bitker.eventservice.events.PeerStateChangedEvent;
import com.bitkermessage.client.messages.messages.SerializedMessage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Created by Matteo on 11/10/2016.
 *
 */
public class Peer implements Comparable<Peer>{

    private InetAddress addr;
    private long connectionTime;
    private int attempt;
    private int port;
    private int timestamp;
    private long service;
    private boolean newVersion;
    private long lastMessage;
    private boolean in;
    private int theirVersion;
    private ConcurrentLinkedQueue<SerializedMessage> pendingMessages;
    private SerializedMessage incompleteMsg;
    private PeerState state;
    private SocketChannel skt;
    private String agent;
    private SelectionKey key;

    public Peer(InetAddress addr,int port){
        attempt = 0;
        newVersion = true;
        pendingMessages = new ConcurrentLinkedQueue<>();
        this.addr = addr;
        this.port = port;
        state = PeerState.CLOSE;
        skt = null;
        incompleteMsg = null;
        setTime();
        connectionTime = 0;
    }

    void setAgent(String s){
        agent = s;
    }

    void setIn(boolean isIn){
        in = isIn;
    }

    public boolean isIn(){
        return in;
    }

    public String getAgent(){
        return agent;
    }

    void setTime(){
        lastMessage = System.currentTimeMillis();
    }

    long getTime(){
        return lastMessage;
    }

    void setSocket(SocketChannel skt){
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

    void setPeerState(PeerState state) {
        if(state == this.state)
            return;
        PeerState old = this.state;
        this.state = state;
        EventService.getInstance().publish(new PeerStateChangedEvent(this,old));
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

    SerializedMessage peekMsg() {
        return pendingMessages.peek();
    }

    SerializedMessage poolMsg() {
        return pendingMessages.poll();
    }

    boolean hasNoPendingMessage() {
        return pendingMessages.isEmpty();
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getTimestamp() {
        return timestamp;
    }


    void setService(long service) {
        this.service = service;
    }

    public long getService() {
        return service;
    }

    void setPort(int port) {
        this.port = port;
    }

    public PeerState getPeerState() {
        return state;
    }

    @Override
    public int compareTo(@NotNull Peer o) {
        int res = this.attempt - o.attempt;
        if(res == 0)
            res = o.timestamp - this.timestamp;
        return res;
    }

    @Override
    public boolean equals(Object obj) {
        if(!obj.getClass().isAssignableFrom(Peer.class))
            return false;
        Peer o = (Peer) obj;
        if(addr.getHostAddress().equals(o.getAddress().getHostAddress()))
            return port == o.getPort();
        return false;
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

    public boolean close(){
        if(state == PeerState.OPEN)
            resetAttempt();
        else
            attempt++;
        setPeerState(PeerState.CLOSE);
        pendingMessages.clear();
        incompleteMsg = null;
        try
        {
            if(this.skt != null)
            {
                this.skt.close();
                Main.openedFiles.decrementAndGet();
            }
        } catch (IOException e)
        {
            this.skt = null;
            e.printStackTrace();
        }
        if(key != null)
            key.cancel();
        key = null;
        Main.listener.selector.wakeup();
        newVersion = true;
        return attempt < 10;
    }

    void resetAttempt() {
        attempt = 0;
    }

    void setKey(SelectionKey key) {
        this.key = key;
    }

    public long getConnectionTime() {
        return connectionTime;
    }

    void setConnectionTime(long connectionTime) {
        this.connectionTime = connectionTime;
    }

    public int theirVersion() {
        return theirVersion;
    }

    void setTheirVersion(int version) {
        theirVersion = version;
    }
}

