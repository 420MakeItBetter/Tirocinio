package Client.network;

import Client.Protocol.Connect;
import Client.Protocol.KeepAlive;
import Client.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

/**
 * Created by Matteo on 12/10/2016.
 */
public class ComputeTask implements Runnable {

    private SocketChannel skt;
    private Peer p;
    private Message m;
    private Logger logger = LoggerFactory.getLogger(ReadTask.class);

    public ComputeTask(SocketChannel skt, Peer p,Message m){
        this.skt = skt;
        this.p = p;
        this.m = m;
    }

    @Override
    public void run() {

        try
        {
            logger.info("Messaggio ricevuto {} da {}",m.getCommand(),p.getAddress());
            if(m instanceof VerAck)
                ;
            else if(m instanceof Version)
                versionResponse((Version) m);
            else if(m instanceof Ping)
                pingResponse((Ping) m);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void pingResponse(Ping m) throws IOException {
        KeepAlive.sendPong(m,skt,p);
    }

    private void versionResponse(Version m) throws ClosedChannelException {
        VerAck ack = new VerAck();
        p.setPeerState(PeerState.OPEN);
        Connect.sendVerAck(ack,skt,p);
    }






}
