package client.commands;

import client.messages.PeerAddress;

/**
 * Created by Matteo on 24/11/2016.
 */
public class AddrStruct {

    public PeerAddress addr;
    public String peer;

    public AddrStruct(PeerAddress addr, String peer) {
        this.addr = addr;
        this.peer = peer;
    }
}