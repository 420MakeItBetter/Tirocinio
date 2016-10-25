package Client.network;

import Client.Main;
import Client.bitio.LittleEndianInputStream;
import Client.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created by Matteo on 11/10/2016.
 *
 */
public class ReadTask implements Runnable{

    private SocketChannel skt;
    private Peer p;
    private SerializedMessage msg;

    public ReadTask(SocketChannel skt,Peer p,SerializedMessage msg) {
        this.skt = skt;
        this.p = p;
        this.msg = msg;
    }

    @Override
    public void run() {
        byte [] command = new byte [12];
        msg.getHeader().rewind();
        if(msg.getPayload() != null)
            msg.getPayload().rewind();
        msg.getHeader().position(4);
        msg.getHeader().get(command);
        msg.setCommand(new String(command).trim());
        msg.getHeader().position(20);
        msg.setChecksum(msg.getHeader().getInt());

        Message m = createMessage(msg);
        ComputeTask task = new ComputeTask(skt,p,m);
        Main.listener.ex.execute(task);
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
            message.read(LittleEndianInputStream.wrap(msg.getPayload()));
            return message;
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;

    }

}
