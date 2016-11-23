package Client.network;



import Client.messages.SerializedMessage;

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
    private int tests;
    private String agent;

    public Peer(InetAddress addr,int port){
        attempt = 0;
        tests = 0;
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
        int res = this.attempt - o.attempt;
        if(res == 0)
            res = o.timestamp - this.timestamp;
        return res;
    }

    public void incrementAttempt() {
        attempt++;
    }


    @Override
    public String toString() {
        return addr.toString()+" "+port+" "+service+" "+state+"\n"+incompleteMsg+"\n"+pendingMessages;
    }

    public void incrementTests() {
        tests++;
    }

    public int getTests() {
        return tests;
    }

    public boolean getVersion() {
        return newVersion;
    }

    public void setVersion(boolean version) {
        this.newVersion = version;
    }
}

