package cs455.scaling.server;


import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

/**
 * Created by MyGarden on 17/3/7.
 */
public class Worker extends Thread{

    private ThreadPoolManager parent;
    private final int packetSize = 8192 ;
    public Task task = null;
    public boolean isWorking;

    private void readTask(){

        //Parse the task
        SocketChannel socketChannel= task.channel;

        //do the read task
        ByteBuffer buf = ByteBuffer.allocate(packetSize);
        int bytesRead;
        while (buf.hasRemaining()) {
            try {
                bytesRead = socketChannel.read(buf);
                if (bytesRead == -1){
                    System.out.println("Connection has lost. Exit");
                    System.exit(-1);
                }
            } catch (IOException ioe){
                System.out.println("Fail to read data");
                System.exit(-1);
            }
        }
        // Now I have got a complete packet, put it into the workToDo list again to do the compute SHA-1 task
        String type = "hash";
        String originalPacket = buf.toString();
        Task nextTask = new Task(type, originalPacket, socketChannel);
        parent.addToWorkToDo(nextTask);


        //declare available
        isWorking = false;
        parent.addToReadyWorker(this);

    }

    private void writeTask(){

        //Parse the task
        String hashedPacket = task.object;
        SocketChannel channel= task.channel;

        //send hashedPacket back to client
        HashPacket backPacket = new HashPacket(hashedPacket.getBytes());

        ByteBuffer buf = ByteBuffer.allocate(40);
        buf.put(backPacket.value);
        buf.flip();

        //send back the hashed packet
        try {
            while (buf.hasRemaining())
                 channel.write(buf);
        } catch (IOException ioe){
            System.out.println("Fail to write buf");
            System.exit(-1);
        }

        //declare available
        isWorking = false;
        parent.addToReadyWorker(this);
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
    private void hashTask(){
        //Parse the task
        String originalPacket = task.object;
        SocketChannel socketChannel= task.channel;

        //do the hash task
        String hashedPacket = SHA1FromBytes(originalPacket.getBytes());

        // Now I have got a hashed packet, put it into the workToDo list again to do the sending(writing) task
        String type = "write";
        Task nextTask = new Task(type, hashedPacket, socketChannel);
        parent.addToWorkToDo(nextTask);

        //declare available
        isWorking = false;
        parent.addToReadyWorker(this);

    }


    public Worker(ThreadPoolManager parent){
        this.parent = parent;
        this.isWorking = false;
    }
    public void run(){
        parent.addToReadyWorker(this);
        while (true){
            if (isWorking){
                if (task.type.equals("read")) {
                    readTask();
                } else if (task.type.equals("write")) {
                    writeTask();
                } else if (task.type.equals("hash")) {
                    hashTask();
                }
            }
            else{
                try {
                    wait();
                } catch (InterruptedException ie) {
                    System.out.println("wait is interrupted in " + Thread.currentThread());
                    System.exit(-1);
                }
            }
        }
    }
}
