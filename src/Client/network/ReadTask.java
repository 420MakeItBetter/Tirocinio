package Client.network;

import Client.Main;
import Client.bitio.LittleEndianInputStream;
import Client.messages.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Random;

/**
 * Created by Matteo on 11/10/2016.
 *
 */
public class ReadTask extends Task{

    private static Random r = new Random();
    private SocketChannel skt;
    private SerializedMessage msg;
    private boolean doClean;

    public ReadTask(SocketChannel skt,Peer p,SerializedMessage msg) {
        this.skt = skt;
        this.p = p;
        this.msg = msg;
        doClean = true;
    }


    @Override
    protected void clean() {
    }

    @Override
    protected void closeResources() {
    }

    @Override
    protected void doTask() throws IOException{
        byte[] command = new byte[12];
        msg.getHeader().rewind();
        if (msg.getPayload() != null)
            msg.flipPayload();
        msg.getHeader().position(4);
        msg.getHeader().get(command);
        msg.setCommand(new String(command).trim());
        msg.getHeader().position(20);
        msg.setChecksum(msg.getHeader().getInt());


        Message m = createMessage(msg);
        if (m != null)
        {
            if (m instanceof UnknownMessage)
                ((UnknownMessage) m).setCommand(msg.getCommand());
            ComputeTask task = new ComputeTask(skt, p, m);
            Main.listener.ex.execute(task);
        }

        if(m instanceof Inventory)
            if(r.nextInt(100) > 60)
            {
                msg.getHeader().rewind();
                msg.flipPayload();
                p.addMsg(msg);
                Main.listener.addChannel(skt, SelectionKey.OP_WRITE | SelectionKey.OP_READ, p);
                doClean = false;
            }
    }


    private Message createMessage(SerializedMessage msg) throws IOException{

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
            default:
                message = new UnknownMessage();
        }
        message.setLength(msg.getSize());
        message.setChecksum(msg.getChecksum());
        LittleEndianInputStream in = LittleEndianInputStream.wrap(msg.getPayload());
        try
        {
            message.read(in);
            in.close();
            return message;
        } catch (IOException | NullPointerException e)
        {
            try
            {
                in.close();
            } catch (IOException e1)
            {}
            System.err.println(msg+"\n"+p);
            if(e instanceof IOException)
                throw e;
            else
                throw new IOException();
        }

    }

}
