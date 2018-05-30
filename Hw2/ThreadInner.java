import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class ThreadInner extends Thread {

    private static List<Float> A = new LinkedList<>();
    private static List<Float> B = new LinkedList<>();

    private boolean isParent;
    private boolean completed;

    private int low;
    private int high;
    private float innerProduct;

    ThreadInner(boolean isParent, int low, int high) {
        this.isParent = isParent;
        this.low = low;
        this.high = high;
    }

    public static void main(String[] args) {
        String fileName = args[0];
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(fileName)));

        String line;
        try {
            while((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(",");
                A.add(Float.valueOf(values[0]));
                B.add(Float.valueOf(values[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int size = A.size();
        ThreadInner parentThread = new ThreadInner(true, 0, size - 1);
//        long startTime = System.nanoTime();
        parentThread.start();
//        long endTime = System.nanoTime();
//        long totalTime = endTime - startTime;
//        System.out.println("Time(ns) : " + totalTime);
    }

    public void run() {
        completed = false;
        if(low == high) {
            innerProduct = A.get(low) * B.get(low);
        } else {
            int mid = (low + high)/2;
            ThreadInner childThread1 = new ThreadInner(false, low, mid);
            childThread1.start();
            ThreadInner childThread2 = new ThreadInner(false, mid + 1, high);
            childThread2.start();

            childThread1.sync();
            childThread2.sync();
            innerProduct = childThread1.getInnerProduct() + childThread2.getInnerProduct();
        }
        completed = true;
        if(isParent) {
            System.out.println(innerProduct);
        }
    }

    private float getInnerProduct() {
        return innerProduct;
    }

    private synchronized void sync() {
            if(!completed) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
    }

}
