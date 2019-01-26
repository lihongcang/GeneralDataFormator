import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {
    public static void main(String[] args){

        BlockingQueue<Map<String, String>> blockingQueueIn = new ArrayBlockingQueue<>(10000);
        BlockingQueue<Map<String,String>> blockingQueueOut = new ArrayBlockingQueue<>(10000);

        //线程调度
        for(int i=0;i<1;i++){
            Producer producer = new Producer(blockingQueueIn,"/config_producer.properties");
            Thread queryThread = new Thread(producer);
            queryThread.setName("producer-"+i+" start");
            queryThread.start();
            System.out.println("producer-"+i+" start");
        }
        for (int i = 0; i < 1; i++) {
            Consumer consumer = new Consumer(blockingQueueIn,blockingQueueOut,"/config_consumer.properties");
            Thread processThread = new Thread(consumer);
            processThread.setName("consumer-" + i+" start");
            processThread.start();
            System.out.println("consumer-"+i+" start");
        }
        for (int i = 0; i < 1; i++) {
            Worker worker = new Worker(blockingQueueOut,"/config_worker.properties");
            Thread workThread = new Thread(worker);
            workThread.setName("worker-" + i+" start");
            workThread.start();
            System.out.println("worker-"+i+" start");
        }

    }

}
