package cs455.scaling.client;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by MyGarden on 17/3/6.
 */
public class Statistics extends Thread {
    private Integer messageSent;
    private Integer messageReceived;
    public Statistics(){
        messageReceived = 0;
        messageSent = 0;
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
    public void run(){
        try {
            sleep(10000);
        } catch (InterruptedException ie){
            System.out.println("Sleep interrupted in statistics");
            System.exit(-1);
        }
        while (true){
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(new Date());
            System.out.println(timeStamp + " Total sent count: " + messageSent + " Total received count: " + messageReceived);
            messageReceived = 0;
            messageSent = 0;
            try {
                sleep(10000);
            } catch (InterruptedException ie){
                System.out.println("Sleep interrupted");
                System.exit(-1);
            }
        }
    }


}
