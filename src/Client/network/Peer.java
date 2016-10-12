package Client.network;



import Client.messages.SerializedMessage;

import java.net.InetAddress;


/**
 * Created by Matteo on 11/10/2016.
 */
public class Peer {

    private InetAddress addr;
    private int port;
    private SerializedMessage msg;
    private PeerState state;

    public Peer(InetAddress addr,int port){
        this.addr = addr;
        this.port = port;
        state = PeerState.HANDSAKE;
        msg = null;
    }

    public SerializedMessage getMsg() {
        return msg;
    }

    public void setMsg(SerializedMessage msg) {
        this.msg = msg;
    }
}
