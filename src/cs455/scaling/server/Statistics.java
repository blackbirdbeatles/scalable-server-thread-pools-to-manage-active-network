package cs455.scaling.server;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by MyGarden on 17/3/8.
 */
public class Statistics extends Thread {
    private Integer messageSent;
    private Integer messageReceived;
    private Integer connection;
    public Statistics(){
        messageReceived = 0;
        messageSent = 0;
        connection = 0;
    }
    public void incrementMessageSent(){
        synchronized (messageSent) {
            messageSent++;
        }
    }
    public void incrementMessageReceived(){
        synchronized (messageReceived) {
            messageReceived++;
        }
    }
    public void incrementConnection(){
        synchronized (connection) {
            connection++;
        }
    }
    public void decrementConnection(){
        synchronized (connection) {
            connection--;
        }
    }
    public void run(){
        try {
            sleep(5000);
        } catch (InterruptedException ie){
            System.out.println("Sleep interrupted in statistics");
            System.exit(-1);
        }
        while (true){
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date());
            synchronized (messageReceived){
                synchronized (messageSent) {
                    System.out.println(timeStamp + " Current Server Throughput: " + String.valueOf((messageSent + messageReceived) * 0.1) + " messages/s, " + " Active Client Connections: " + connection);
                    messageReceived = 0;
                    messageSent = 0;
                }
            }
            try {
                sleep(5000);
            } catch (InterruptedException ie){
                System.out.println("Sleep interrupted");
                System.exit(-1);
            }
        }
    }
}
