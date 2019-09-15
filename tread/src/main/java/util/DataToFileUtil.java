package util;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DataToFileUtil<T> {

    private static class SingletonHoder {
        private static DataToFileUtil instance = new DataToFileUtil();
    }

    public static DataToFileUtil getInstance() {
        return SingletonHoder.instance;
    }

    Supplier<T> dataSupplier;
    Consumer<T> dataConsumer;

    private static final AtomicInteger count = new AtomicInteger(0);
    public static void main(String[] args) throws IOException {
        List<Integer> integers = Arrays.asList(1, 2, 3, 4);
        BufferedWriter writer = new BufferedWriter(new FileWriter("1.csv"));
        LinkedBlockingDeque<Task> blockingDeque = new LinkedBlockingDeque<>();

        Random random = new Random();
        ExecutorService threadPool = Executors.newCachedThreadPool();
        Supplier<String> stringSupplier = () -> {
            if(count.get() >= 1000) {
                System.out.println("=======");
                return null;
            }
            count.getAndIncrement();
            return Thread.currentThread().getName() + "_" + count.get() + "\r\n";
        };
        Consumer<Task> intConsumer = o -> {
            try {
                writer.write(o.data);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        ProducerThread p1 = new ProducerThread(blockingDeque, "pro1");
        ProducerThread p2 = new ProducerThread(blockingDeque, "pro2");
        p1.start();
        p2.start();
        ConsumerThread c1 = new ConsumerThread(blockingDeque, intConsumer);
        p1.stopThread();
        p2.stopThread();
        c1.start();

    }

    public static class Task {
        private String data;
        private int num;
        public Task(String data) {
            this.data = data;
        }

    }
    public static class ProducerThread extends Thread {
        private volatile boolean isRunnig = true;
        private BlockingDeque<Task> blockingDeque;

        public ProducerThread(BlockingDeque<Task> blockingDeque, String name) {
            this.blockingDeque = blockingDeque;
            this.setName(name);
        }

        @Override
        public void run() {
            try {
                Random random = new Random();
                while (isRunnig) {
                    if (count.get() > 1000) {
                        System.out.println("=======");
                        blockingDeque.put(new Task(null));
                        isRunnig = false;
                        break;
                    }
//                    if (!blockingDeque.offer(data, 2, TimeUnit.SECONDS)) {
//                        System.err.println("提交数据失败");
//                    }
                    Task task = new Task(getName() + " " + count.get() + "\r\n");
                    blockingDeque.put(task);
                    count.getAndIncrement();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }

        public void stopThread() {
            isRunnig = false;
        }
    }

    public static class ConsumerThread extends Thread {
        private volatile boolean isRunnig = true;
        private final String name = "consumer";
        private BlockingDeque<Task> blockingDeque;
        private Consumer<Task> dataConsumer;

        public ConsumerThread(BlockingDeque<Task> blockingDeque, Consumer<Task> dataConsumer) {
            this.blockingDeque = blockingDeque;
            this.dataConsumer = dataConsumer;
        }

        @Override
        public void run() {
            try {
                while (isRunnig && !Thread.currentThread().isInterrupted()) {
                    Task data = blockingDeque.take();
                    if (data.data == null) {
                        isRunnig = false;
                        break;
                    }
                    dataConsumer.accept(data);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }

        }
    }
}