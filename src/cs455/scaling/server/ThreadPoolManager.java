package cs455.scaling.server;

import java.nio.channels.SocketChannel;
import java.util.LinkedList;

/**
 * Created by MyGarden on 17/3/5.
 */
public class ThreadPoolManager extends Thread {

    private int poolSize;
    private LinkedList<Task> workToDo;
    private LinkedList<Worker> readyWorkers;


    public void addToReadyWorker(Worker worker){
        synchronized (readyWorkers){
            readyWorkers.add(worker);
        }
    }
    public void addToWorkToDo(Task task){
        synchronized (workToDo){
            workToDo.add(task);
        }
    }
    public void deleteFromReadyWorker(Worker worker){
        synchronized (readyWorkers){
            readyWorkers.remove(worker);
        }
    }
    public void deleteFromWorkToDo(Task task){
        synchronized (workToDo){
            workToDo.remove(task);
        }
    }


    private void generateThread(){
        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker(this);
            worker.start();
        }
    }

    public ThreadPoolManager(int poolSize){
        this.poolSize = poolSize;
        workToDo = new LinkedList<>();
        readyWorkers = new LinkedList<>();
    }

    public void run(){
        generateThread();
        while (true){
            synchronized (workToDo) {
                if (!workToDo.isEmpty()) {
                    Task task = workToDo.getFirst();
                    synchronized (readyWorkers) {
                        if (!readyWorkers.isEmpty()) {
                            Worker worker = readyWorkers.getFirst();

                            deleteFromWorkToDo(task);
                            deleteFromReadyWorker(worker);
                            worker.isWorking = true;
                            worker.task = task;
                            notifyAll();
                        }
                    }
                }
            }
        }
    }
}
