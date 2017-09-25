package client.api;

import client.Main;
import client.eventservice.filters.StateFilter;
import client.protocol.Connect;
import client.protocol.ProtocolUtil;
import client.eventservice.EventService;
import client.eventservice.events.*;
import client.eventservice.filters.Filter;
import client.eventservice.filters.MsgFilter;
import client.eventservice.filters.PeerFilter;
import client.eventservice.subscribers.*;
import client.network.Peer;
import client.network.PeerState;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by machiara on 06/03/17.
 */
public class PublicInterfaceReader implements Runnable {

    ByteBuffer msg;
    ApiClientData data;

    public PublicInterfaceReader(ByteBuffer msg, ApiClientData data) {
        this.msg = msg;
        this.data = data;
        this.msg.clear();
    }

    @Override
    public void run() {
        int type;
        long id = -1;
        try
        {
            type = msg.getInt();
            id = msg.getLong();
            data.addId(id);
            Subscriber sub = null;
            Filter filter = null;
            System.out.println(type+" "+id);
            switch (type)
            {
                case 1:
                    sub = new MessageReceivedSubscriber();
                    sub.id = id;
                    sub.data = data;
                    int n = msg.getInt();
                    System.out.println(n);
                    Set<String> msgtypes = new HashSet<>(n);
                    for (int i = 0; i < n; i++)
                    {
                        byte[] bytes = new byte[12];
                        this.msg.get(bytes);
                        String s = new String(bytes).trim();
                        msgtypes.add(s);
                        System.out.println(s);
                    }
                    byte b = msg.get();
                    System.out.println(b);
                    ((MessageReceivedSubscriber) sub).what = b;
                    filter = new MsgFilter(msgtypes);
                    EventService.getInstance().subscribe(MessageReceivedEvent.class, filter, sub);
                    ByteBuffer msg = ack(0, id);
                    data.addMsg(msg);
                    break;

                case 2:
                    sub = new MessageReceivedSubscriber();
                    sub.id = id;
                    sub.data = data;
                    n = this.msg.getInt();
                    Set<String> peers = new HashSet<>(n);
                    for(int i = 0; i < n; i++)
                    {
                        byte [] bytes = new byte [16];
                        this.msg.get(bytes);
                        String s = InetAddress.getByAddress(bytes).getHostAddress();
                        peers.add(s);
                    }
                    b = this.msg.get();
                    ((MessageReceivedSubscriber) sub).what = b;
                    filter = new PeerFilter(peers);
                    EventService.getInstance().subscribe(MessageReceivedEvent.class,filter,sub);
                    msg = ack(0, id);
                    data.addMsg(msg);
                    sub = new PeerStateSubscriber();
                    sub.id = id;
                    sub.data = data;
                    EventService.getInstance().subscribe(PeerStateChangedEvent.class,filter,sub);
                    for(String s : peers)
                    {
                        msg = ByteBuffer.allocate(4+4+8+16+1);
                        msg.putInt(4+8+16+1);
                        msg.putInt(5);
                        msg.putLong(id);
                        if(Main.peers.containsKey(s))
                        {
                            Peer p = Main.peers.get(s);
                            if(p.getAddress().getAddress().length == 4)
                                msg.put(new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                                        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                                        (byte)0x00,(byte)0xFF,(byte)0xFF});
                            msg.put(p.getAddress().getAddress());
                            switch (p.getState())
                            {
                                case CLOSE:
                                    msg.put((byte) 0);
                                    Connect.connect(p.getAddress(),8333,p);
                                    break;
                                case HANDSHAKE:
                                    msg.put((byte) 1);
                                    break;
                                case OPEN:
                                    msg.put((byte) 2);
                                    break;
                            }
                        }
                        else
                        {
                            Peer p = new Peer(InetAddress.getByName(s),8333);
                            Main.peers.put(s,p);
                            Connect.connect(p.getAddress(),8333,p);
                            if(p.getAddress().getAddress().length == 4)
                                msg.put(new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                                        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                                        (byte)0x00,(byte)0xFF,(byte)0xFF});
                            msg.put(p.getAddress().getAddress());
                            msg.put((byte) 0);
                        }
                        data.addMsg(msg);
                    }

                    sub = new ConnectSubscriber();
                    sub.id = id;
                    sub.data = data;
                    EventService.getInstance().subscribe(ConnectedEvent.class,filter,sub);
                    EventService.getInstance().subscribe(NotConnectedEvent.class,filter,sub);
                    break;

                case 3:
                    sub = new MessageSentSubscriber();
                    sub.id = id;
                    sub.data = data;
                    byte [] arr = new byte [16];
                    this.msg.get(arr);
                    String s = InetAddress.getByAddress(arr).getHostAddress();
                    HashSet<String> set = new HashSet<>();
                    set.add(s);
                    filter = new PeerFilter(set);
                    EventService.getInstance().subscribe(MessageSentEvent.class,filter,sub);
                    msg = ack(0,id);
                    data.addMsg(msg);
                    sub = new PeerStateSubscriber();
                    sub.id = id;
                    sub.data = data;
                    EventService.getInstance().subscribe(PeerStateChangedEvent.class,filter,sub);
                    msg = ByteBuffer.allocate(4+4+8+16+1);
                    msg.putInt(4+8+16+1);
                    msg.putInt(5);
                    msg.putLong(id);
                    if(Main.peers.containsKey(s))
                    {
                        Peer p = Main.peers.get(s);
                        if(p.getAddress().getAddress().length == 4)
                            msg.put(new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                                    (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                                    (byte)0x00,(byte)0xFF,(byte)0xFF});
                        msg.put(p.getAddress().getAddress());
                        switch (p.getState())
                        {
                            case CLOSE:
                                msg.put((byte) 0);
                                Connect.connect(p.getAddress(),8333,p);
                                break;
                            case HANDSHAKE:
                                msg.put((byte) 1);
                                break;
                            case OPEN:
                                msg.put((byte) 2);
                                ByteBuffer header = ByteBuffer.allocate(24);
                                while(header.hasRemaining())
                                    header.put(this.msg.get());
                                ByteBuffer payload = ByteBuffer.allocate(this.msg.remaining());
                                payload.put(this.msg);
                                ProtocolUtil.sendMessage(header,payload,p.getSocket(),p,id);
                        }
                    }
                    else
                    {
                        Peer p = new Peer(InetAddress.getByName(s),8333);
                        Main.peers.put(s,p);
                        Connect.connect(p.getAddress(),8333,p);
                        if(p.getAddress().getAddress().length == 4)
                            msg.put(new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                                    (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                                    (byte)0x00,(byte)0xFF,(byte)0xFF});
                        msg.put(p.getAddress().getAddress());
                        msg.put((byte) 0);
                    }
                    data.addMsg(msg);

                    sub = new ConnectSubscriber();
                    sub.id = id;
                    sub.data = data;
                    EventService.getInstance().subscribe(ConnectedEvent.class,filter,sub);
                    EventService.getInstance().subscribe(NotConnectedEvent.class,filter,sub);
                    break;
                case 4:

                    sub = new MessageSentToAllSubscriber();
                    sub.id = id;
                    sub.data = data;
                    int number = this.msg.getInt();
                    ((MessageSentToAllSubscriber) sub).n = number;
                    int pos = this.msg.position();
                    Set<String> peerset = new HashSet<>(number);
                    filter = new MsgFilter(id);
                    EventService.getInstance().subscribe(MessageSentEvent.class,filter,sub);
                    msg = ack(0,id);
                    data.addMsg(msg);
                    for(Peer p : Main.peers.values())
                    {
                        if(number <= 0)
                            break;
                        if(p.getState() == PeerState.OPEN)
                        {
                            ByteBuffer header = ByteBuffer.allocate(24);
                            while (header.hasRemaining())
                                header.put(this.msg.get());
                            ByteBuffer payload = ByteBuffer.allocate(this.msg.remaining());
                            payload.put(this.msg);
                            this.msg.position(pos);
                            ProtocolUtil.sendMessage(header,payload,p.getSocket(),p,id);
                            peerset.add(p.getAddress().getHostAddress());
                            number--;
                        }
                    }
                    sub = new ChangePeerSubscriber();
                    sub.data = data;
                    sub.id = -1;
                    ByteBuffer header = ByteBuffer.allocate(24);
                    while (header.hasRemaining())
                        header.put(this.msg.get());
                    ByteBuffer payload = ByteBuffer.allocate(this.msg.remaining());
                    payload.put(this.msg);
                    filter = new PeerFilter(peerset);
                    ((ChangePeerSubscriber) sub).peers = peerset;
                    ((ChangePeerSubscriber) sub).header = header;
                    ((ChangePeerSubscriber) sub).payload = payload;
                    ((ChangePeerSubscriber) sub).reqid = id;
                    EventService.getInstance().subscribe(PeerStateChangedEvent.class,filter,sub);
                    break;

                case 5 :
                    msg = ack(0,id);
                    data.addMsg(msg);
                    ByteArrayOutputStream str = new ByteArrayOutputStream();
                    DataOutputStream stream = new DataOutputStream(str);
                    int size = 0;
                    for(Peer p : Main.peers.values())
                        if(p.getState() == PeerState.OPEN)
                        {
                            if(p.getAddress().getAddress().length == 4)
                                stream.write(new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                                        (byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
                                        (byte)0x00,(byte)0xFF,(byte)0xFF});
                            stream.write(p.getAddress().getAddress());
                            stream.writeByte((int) p.getService());
                            stream.writeByte(p.isIn() ? (byte) 1 : (byte) 0);
                            stream.writeInt(p.getAgent().length());
                            stream.write(p.getAgent().getBytes());
                            size++;
                        }
                    msg = ByteBuffer.wrap(str.toByteArray());
                    ByteBuffer tmp = ByteBuffer.allocate(4+4+8+4);
                    tmp.putInt(msg.limit()+4+8+4);
                    tmp.putInt(4);
                    tmp.putLong(id);
                    tmp.putInt(size);
                    data.addMsg(tmp,msg);
                    break;
                case 6 :
                    msg = ack(0,id);
                    data.addMsg(msg);
                    long toTerminate = this.msg.getLong();
                    EventService.getInstance().unsubscribe(data,toTerminate);
                    break;
                case 8 :
                    msg = ack(0,id);
                    data.addMsg(msg);
                    sub = new PeerStateChangedSubscriber();
                    sub.id = id;
                    sub.data = data;
                    byte b1 = this.msg.get();
                    switch (b1)
                    {
                        case 0 :
                            filter = null;
                            break;
                        case 1 :
                            filter = new StateFilter(PeerState.OPEN);
                            break;
                        case 2 :
                            filter = new StateFilter(PeerState.CLOSE);
                            break;
                        case 3 : filter = new StateFilter(PeerState.HANDSHAKE);
                    }
                    EventService.getInstance().subscribe(PeerStateChangedEvent.class,filter,sub);
                    break;
                case 9 :
                    msg = ack(0,id);
                    data.addMsg(msg);
                    sub = new ConnectionSubscriber();
                    sub.id = id;
                    sub.data = data;
                    EventService.getInstance().subscribe(ConnectedEvent.class,null,sub);
                    break;
                case 10:
                    msg = ack(0,id);
                    data.addMsg(msg);
                    sub = new NewMessageSentSubscriber();
                    sub.id = id;
                    sub.data = data;
                    EventService.getInstance().subscribe(MessageSentEvent.class,null,sub);
                    break;
                default:
                    ByteBuffer resp = ack(1, id);
                    data.addMsg(resp);
            }
        }catch (Exception e){
            e.printStackTrace();
            ByteBuffer msg = ack(2,id);
            data.addMsg(msg);
        }
    }


    private ByteBuffer ack(int cause,long id){
        ByteBuffer msg = id == -1 ? ByteBuffer.allocate(4+4+4) : ByteBuffer.allocate(4+4+4+8);
        msg.putInt(msg.limit()-4);
        msg.putInt(1);
        msg.putInt(cause);
        if(id != -1)
            msg.putLong(id);
        return msg;
    }
}
