package Client.network;

import Client.bitio.LittleEndianInputStream;
import Client.messages.*;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * Created by Matteo on 12/10/2016.
 */
public class ComputeTask implements Runnable {

    private SelectionKey key;
    private Selector selector;

    public ComputeTask(SelectionKey key, Selector selector){
        this.key = key;
        this.selector = selector;
    }

    @Override
    public void run() {

        Peer peer = (Peer) key.attachment();
        Message m = createMessage(peer.getMsg());


    }

    private Message createMessage(SerializedMessage msg){

        Message message = null;
        switch (msg.getCommand().toLowerCase())
        {
            case "addr" :
                message = new Address();
                break;
            case "alert" :
                message = new Alert();
                break;
            case "block" :
                message = new Block();
                break;
            case "feefilter" :
                message = new FeeFilter();
                break;
            case "filteradd" :
                message = new FilterAdd();
                break;
            case "filterclear" :
                message = new FilterClear();
                break;
            case "filterload" :
                message = new FilterLoad();
                break;
            case "getaddr" :
                message = new GetAddress();
                break;
            case "getblocks" :
                message = new GetBlocks();
                break;
            case "getdata" :
                message = new GetData();
                break;
            case "getheaders" :
                message = new GetHeaders();
                break;
            case "headers" :
                message = new Header();
                break;
            case "inv" :
                message = new Inventory();
                break;
            case "mempool" :
                message = new MemoryPool();
                break;
            case "merkleblock" :
                message = new MerkleBlock();
                break;
            case "notfound" :
                message = new NotFound();
                break;
            case "ping" :
                message = new Ping();
                break;
            case "pong" :
                message = new Pong();
                break;
            case "reject" :
                message = new Reject();
                break;
            case "sendcmpct" :
                message = new SendCMPCT();
                break;
            case "sendheaders" :
                message = new SendHeaders();
                break;
            case "tx" :
                message = new Transaction();
                break;
            case "verack" :
                message = new VerAck();
                break;
            case "version" :
                message = new Version();
                break;
        }

        message.setLength(msg.getSize());
        message.setChecksum(msg.getChecksum());
        try
        {
            message.read(LittleEndianInputStream.wrap(msg.getBuffer()));
            return message;
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;

    }


}
