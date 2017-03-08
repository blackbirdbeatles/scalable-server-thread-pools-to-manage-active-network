package cs455.scaling.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * Created by MyGarden on 17/3/5.
 */
public class Client {
    private SocketChannel socketChannel;
    private Selector selector;
    private final int packetSize = 8192;
    private LinkedList<String> hashList;
    private Statistics statistics;

    public Client(){

        //create socket channel
        try {
            socketChannel = SocketChannel.open();
            //I can try java NIO
            //   socketChannel.connect(new InetSocketAddress(serverHost, serverRate));

        }
        catch (IOException ioe){
            System.out.println("Fail to open socket.");
            return;
        }


        //create selector
        try {
            selector = Selector.open();
        }
        catch (IOException ioe){
            System.out.println("Fail to open selector");
        }
        this.hashList = new LinkedList<>();
        this.statistics = new Statistics();

    }
    public void connectToServer_WaitForResponse_createPacketSender(String serverHost, int serverPort, int messageRate){

        //using NIO. register socketChannel to selector with interest connectable and readable
        SelectionKey Key;
        try {
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }
        catch (IOException ioe){
            System.out.println("Fail to set socketChannel non-blocking");
            return;
        }

        //connect to Server
        try {
            socketChannel.connect(new InetSocketAddress(serverHost, serverPort));
        }
        catch (IOException ioe){
            System.out.println("Fail to connect to Server");
            return;
        }


        //check the interest   connectable or readable
        while(true){
            try {
                int i = selector.select();
            } catch (IOException ioe){
                System.out.println("fail to select");
                System.exit(-1);
            }
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator iterator = selectedKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = (SelectionKey)iterator.next();
                if (key.isConnectable()){
                    //finish connection

                    try {
                        ((SocketChannel)key.channel()).finishConnect();
                    } catch (IOException ioe) {
                        System.out.println("fail to connect to server");
                        return;
                    }

                    key.interestOps(SelectionKey.OP_READ);
                    //create packet sender
                    PacketSender packetSender = new PacketSender(this.socketChannel, messageRate, packetSize, hashList, statistics);
                    packetSender.start();
                }

                if (key.isReadable()){
                    ByteBuffer buf = ByteBuffer.allocate(40);
                    try {
                        int bytesRead;
                        while (buf.remaining()>0) {
                            bytesRead = socketChannel.read(buf);
                            if (bytesRead == -1){
                                System.out.println("Connection has lost. Exit");
                                System.exit(-1);
                            }
                        }

                    } catch (IOException ioe){
                        System.out.println("Fail to read data");
                        System.exit(-1);
                    }
                    statistics.incrementMessageReceived();
                    // Now I have got a complete hashcode
                    buf.flip();
                    byte[] hc= new byte[40];
                    buf.get(hc);
                    String hashCode = new String(hc);
//                    System.out.println("received hash: "+ hashCode);
                    synchronized (hashList) {
                        if (hashList.contains(hashCode)) {
                            hashList.remove(hashCode);
                        }
                        else{
                            System.out.println("Wrong hashcode");
                        }
                    }
                }
                iterator.remove();
            }
        }
    }



    public static void main(String[] arg){
        String serverHost = arg[0];
        int serverPort;
        int messageRate;


        //extract arguments from the prompt
        try {
            serverPort = Integer.parseInt(arg[1]);
            messageRate = Integer.parseInt(arg[2]);
        }
        catch (NumberFormatException nfe){
            System.out.println("wrong server port or rate.");
            return;
        }


        Client client = new Client();
        client.statistics.start();
        client.connectToServer_WaitForResponse_createPacketSender(serverHost,serverPort,messageRate);







    }
}
