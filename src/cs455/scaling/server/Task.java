package cs455.scaling.server;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Created by MyGarden on 17/3/7.
 */
public class Task {

    public String type;
    public byte[] object;
    public SocketChannel channel;
    public SelectionKey key;


    public Task(String type, byte[] object, SocketChannel channel, SelectionKey key){
        this.type = type;
        this.object = object;
        this.channel = channel;
        this.key = key;
    }
}
