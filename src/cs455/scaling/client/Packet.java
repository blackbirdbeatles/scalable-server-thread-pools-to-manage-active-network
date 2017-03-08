package cs455.scaling.client;

import java.util.Random;

/**
 * Created by MyGarden on 17/3/5.
 */
public class Packet {
    public byte[] value;
    Random random = new Random();
    public Packet(final int packetSize){
        value = new byte[packetSize];
        try{
            random.nextBytes(value);
        } catch (NullPointerException npe){
            System.out.println("packet generation error");
        }
    }

}
