package cs455.scaling.server;

import java.nio.channels.SocketChannel;

/**
 * Created by MyGarden on 17/3/7.
 */
public class Task {

    public String type;
    public String object;
    public SocketChannel channel;


    public Task(String type, String object, SocketChannel channel){
        this.type = type;
        this.object = object;
        this.channel = channel;
    }
}
