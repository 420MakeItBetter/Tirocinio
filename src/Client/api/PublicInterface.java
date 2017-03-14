package Client.api;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Matteo on 03/03/17.
 *
 * Api version 1.0
 *
 * All message are in network byte order
 *
 * Message consists of 4 byte read as unsigned int that indicates the length of the payload of the message followed by
 * the message it self.
 *
 * There are 7 kind of message that can be sent to the listener:
 *
 * 1)"listen" type which can be used to know all the message of a kind that this listener receive.
 * 2)"lfrom" type which can be used to listen all the message received by a specific peer.
 * 3)"sendto" type which can be used to send a message to a specific peer.
 * 4)"sendtoall" type which can be used to send a message to a number of peers.
 * 5)"list" type which can be used to receive the list of connected peers.
 * 6)"terminate" type which can be used to stop a previous requested command.
 * 7)"stat" type which can be used to receive statistics about the messages received.
 *
 * The payload is made of an int which indicates the type of message sent followed by
 * an 8 byte long used to indicate the id of the request from the client, followed by the syntax of the message sent
 *
 * the id must be different for every request sent by the user or unexpected behaviour will occur.
 *
 * Follows the syntax of all the message type:
 *
 * 1) "listen":
 *
 * int n: number of type of message to listen for.
 * char [n][12]: array of n strings of 12 char, each string is a name of a bitcoin message type.
 * byte all: which indicates if you are interested in receive all the message, just the header or just the payload:
 * 0-> header and payload.
 * 1-> just the header.
 * 2-> just the payload.
 *
 * 2) "lfrom":
 *
 * int n: number of peers to listen from.
 * char [n][16]: array of n string of 16 char, each string is an ip address of a peer.
 * byte all: which indicates if you are interested in receive all the message, just the header or just the payload:
 * 0-> header and payload.
 * 1-> just the header.
 * 2-> just the payload.
 *
 * 3) "sendto":
 *
 * char [16]: the ip address of the peer to send the message to.
 * next x byte: the message to send to the peer, first 24 byte the header followed by the payload.
 *
 * 4) "sendtoall":
 *
 * int n: the number of peers to send the message to.
 * next x byte: the message to send to the peers, first 24 byte the header followed by the payload.
 *
 * 5) "list":
 *
 * this message has no payload.
 *
 * 6) "terminate":
 *
 * 8 byte long, the id of the previous started request.
 *
 * 7)"stat":
 *
 * TO BE DEFINED.
 *
 *
 * Listener's messages are different from the client's ones, but the syntax is the same, an int which indicates length
 * of the message followed by an int which indicates the type of the message sent, followed by 8 byte long which is
 * the id sent by the client, followed by the payload of the message.
 *
 *
 * The listener will respond with an acknowledge message to inform the client that he has received the message and will process
 * then it will send the results when they are ready.
 *
 * The acknowledge message doesn't have the id of the received message, after the message type, if present (there may
 * have been an error reading the id) is put after the int which indicates the result of the reading
 *
 * acknowledge id 1:
 *
 * int n: which indicates if everything was fine or not:
 * 0-> everything ok.
 * 1-> unknown type message.
 * 2-> syntax error in the message.
 * 8 byte id: the id of the received message, present if was able to read it
 *
 * msg received id 2:
 *
 * sent when a type of message requested has arrived:
 *
 * 16 byte: textual representation of the ip address of the peer which sent the message.
 * next x byte: the message, has indicated in the listen or lfrom message, so it can be only the header or the payload.
 *
 * msg sent id 3:
 *
 * sent when the message sent in a previous "sendto" or "sendtoall" message has been sent.
 *
 * peers list id 4:
 *
 * sent in response to a "list" message:
 *
 * the response is an array of a peer structure which is formed by
 *
 * 16 byte which is the ip address of the peer.
 * 1 byte which is the service of the peer
 * 1 byte that indicates who started the connection:
 * 0 -> the listener connected to the peer,
 * 1 -> the peer connected to the listener.
 * int l: length of the peer agent.
 * char [l] agent: the user agent of the peer
 *
 *
 * int n: the number of peers sent.
 * peer [n]: n peer.
 *
 * state id 5:
 *
 * sent in response to a "sendto" or "lfrom" message or during the execution of this command, indicates the state of one peer,
 * if the state is CLOSED in the moment of the request, an attempt will be made to establish a connection.
 *
 * the first state message received will have just one byte that indicates the current peer state
 *
 * byte [16] ip: the representation of the ip address of the peer.
 * byte oldState: indicate the previous state of the observed peer.
 * 0-> CLOSED.
 * 1-> HANDSHAKE.
 * 2-> OPEN.
 * byte newState: indicate the new state of the observed peer.
 * 0-> CLOSED.
 * 1-> HANDSHAKE.
 * 2-> OPEN.
 *
 * connect id 6:
 *
 * sent when an attempt to connect to the peer indicated in the "sendto" or "lfrom" message is made.
 *
 * byte [16] ip: the representation of the ip address of the peer.
 * byte result: indicate if the result of the connection attempt
 * 0-> couldn't connect.
 * 1-> connected.
 *
 */
public class PublicInterface implements Runnable {

    Selector selector;
    Executor ex;

    public PublicInterface(){
        try
        {
            selector = Selector.open();
            ServerSocketChannel srv = ServerSocketChannel.open();
            srv.bind(new InetSocketAddress(InetAddress.getByName("131.114.88.218"),1994));
            srv.configureBlocking(false);
            srv.register(selector, SelectionKey.OP_ACCEPT);
            ex = Executors.newCachedThreadPool();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void registerChannel(SocketChannel skt,int interest,Object attachment) throws ClosedChannelException {
        skt.register(selector,interest,attachment);
        selector.wakeup();
    }

    @Override
    public void run() {

        while (true)
        {
            try
            {
                selector.selectedKeys().clear();
                selector.select();
                for(SelectionKey k : selector.selectedKeys())
                {
                    if(k.isAcceptable())
                    {
                        ServerSocketChannel srv = (ServerSocketChannel) k.channel();
                        SocketChannel skt = srv.accept();
                        skt.configureBlocking(false);
                        skt.register(selector,SelectionKey.OP_READ,new ApiClientData(skt));
                    }
                    else if(k.isReadable())
                    {
                        ApiClientData data = (ApiClientData) k.attachment();
                        data.read();
                    }
                    else if(k.isWritable())
                    {
                        ApiClientData data = (ApiClientData) k.attachment();
                        data.write();
                    }
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }
}
