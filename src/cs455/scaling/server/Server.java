package cs455.scaling.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

/**
 * Created by MyGarden on 17/3/5.
 */
public class Server {
    int port;
    public Server(int port){
        this.port = port;
    }
    public static void main(String[] args){
        int port = -1;
        try{
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException nfe){
            System.out.println("Wrong format of port number");
            System.exit(-1);
        }
        Server server = new Server(port);

        //create server socket channel, register it to the selector
        ServerSocketChannel serverSocketChannel;
        Selector selector;
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();

            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);

            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException ioe){
            System.out.println("Fail to open server Socket Channel");
            System.exit(-1);
        }

        //accept Socket Channel




    }
}
