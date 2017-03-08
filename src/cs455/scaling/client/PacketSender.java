package cs455.scaling.client;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

/**
 * Created by MyGarden on 17/3/5.
 */
public class PacketSender extends Thread {
    private SocketChannel channel;
    private Statistics statistics;
    private int packetSize;
    private int messageRate;
    LinkedList<String> hashList;
    public PacketSender(SocketChannel socketChannel, int messageRate, int packetSize, LinkedList<String> hashList, Statistics statistics){
        channel = socketChannel;
        this.statistics = statistics;
        this.packetSize = packetSize;
        this.hashList = hashList;
        this.messageRate =messageRate;
    }

    public String SHA1FromBytes(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA1");
            byte[] hash = digest.digest(data);
            BigInteger hashInt = new BigInteger(1, hash);
            String result = hashInt.toString(16);
            while (result.length()!=40)
                result  = "0" + result;
            return result;
        } catch (NoSuchAlgorithmException nsae){
            System.out.println("No such algorithm SHA1");
            System.exit(-1);
        }
        return null;
    }
    public void run(){
        while(true){
            //send packets according to messageRate
            Packet packet = new Packet(packetSize);

            ByteBuffer buf = ByteBuffer.allocate(packetSize);
            buf.put(packet.value);
//            System.out.println("send p: "+ new String(packet.value));
            buf.flip();
            //Firstly, convert buf to buffer array, then compute its hashcode, and add hash to hashList
            String hashCode = SHA1FromBytes(packet.value);
//            System.out.println("ready to send: " + hashCode);
            synchronized (hashList){
                hashList.add(hashCode);
            }
            try {
                //Send packets
                while (buf.hasRemaining()) {
                    int bytesWritten = channel.write(buf);
                }
                statistics.incrementMessageSent();
            } catch (IOException ioe){
                System.out.println("Fail to write buf");
                System.exit(-1);
            }

            //sleep
            //TODO: If you have time to improve this situation, welcome
            try {
                this.sleep(1000 / messageRate);
            } catch (InterruptedException ie){
                System.out.println("Sending has been interrupted");
                System.exit(-1);
            }


        }
    }

}

