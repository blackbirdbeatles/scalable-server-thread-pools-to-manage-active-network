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
    private Statistics statistics;


    public void addToReadyWorker(Worker worker){
        synchronized (readyWorkers){
            readyWorkers.add(worker);
            if (readyWorkers.size()==1)
                readyWorkers.notify();
        }
    }
    public void addToWorkToDo(Task task){
        synchronized (workToDo){
            workToDo.add(task);
            if (workToDo.size()==1)
                workToDo.notify();
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

    public boolean contains(Worker worker){
        synchronized (readyWorkers) {
            return readyWorkers.contains(worker);
        }
    }

    private void generateThread(){
        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker(this, statistics);
            worker.start();
        }
    }

    public ThreadPoolManager(int poolSize, Statistics statistics){
        this.poolSize = poolSize;
        workToDo = new LinkedList<>();
        readyWorkers = new LinkedList<>();
        this.statistics = statistics;
    }

    public void run(){
        generateThread();
        while (true){
            Task task;
            synchronized (workToDo) {
                //把if变成了while 虽然没想清楚为什么
                while (workToDo.isEmpty()) {
                    try {
                        workToDo.wait();
                    } catch (InterruptedException ie) {
                        System.out.println("workToDo wait is interrupted");
                        System.exit(-1);
                    }
                }
                task = workToDo.getFirst();
                deleteFromWorkToDo(task);
            }
            Worker worker;
            synchronized (readyWorkers) {
                while (readyWorkers.isEmpty()) {
                    try {
                        readyWorkers.wait();
                    } catch (InterruptedException ie) {
                        System.out.println("readyWorkers wait is interrupted");
                        System.exit(-1);
                    }
                }
                worker = readyWorkers.getFirst();
                deleteFromReadyWorker(worker);
            }
                synchronized (worker) {
                    worker.task = task;
                    worker.notify();
                }






        }
    }
}
