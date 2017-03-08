package cs455.scaling.server;

import cs455.scaling.client.PacketSender;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by MyGarden on 17/3/5.
 */
public class Server {
    int port;
    public Server(int port){
        this.port = port;
    }
    public static void main(String[] args){

        //extract port & poolSize from command
        int port = -1;
        int poolSize = 0;
        try{
            port = Integer.parseInt(args[0]);
            poolSize = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe){
            System.out.println("Wrong format of port number");
            System.exit(-1);
        }


        //create server socket channel & selector & ThreadPoolManager, register it to the selector
        Server server = new Server(port);
        Statistics statistics = new Statistics();
        statistics.start();
        ServerSocketChannel serverSocketChannel;
        Selector selector;
        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();

            ThreadPoolManager threadPoolManager = new ThreadPoolManager(poolSize, statistics);
            threadPoolManager.start();

            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);

            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true){

                //TODO: watch out the workToDo was repeatedly add to the list

                //Select what is available, the option firstly is only accept, and then more socketChannel come
                try {
                    int i = selector.select();
                } catch (IOException ioe){
                    System.out.println("fail to select");
                    System.exit(-1);
                }
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator iterator = selectedKeys.iterator();


                //Iterate any availabe channel
                while (iterator.hasNext()) {
                    SelectionKey key = (SelectionKey)iterator.next();
                    if (key.isAcceptable()){
                            SocketChannel socketChannel = serverSocketChannel.accept();
                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector, SelectionKey.OP_READ,"");
                            statistics.incrementConnection();
                    } else
                    if (key.attachment().equals(""))
                        if (key.isReadable()) {
                            key.attach("already");
                            Task task = new Task("read", null, (SocketChannel) key.channel(),key);
                            threadPoolManager.addToWorkToDo(task);
                        }
                    iterator.remove();
                }
            }


        } catch (IOException ioe){
            System.out.println("Fail to open server Socket Channel");
            System.exit(-1);
        }

    }
}
