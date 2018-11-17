package tech.qiwang;

import java.util.concurrent.atomic.AtomicInteger;

public class MutiThreadTest implements Runnable{

    public static int i = 0;
    public static volatile int vI = 0;
    public static AtomicInteger aI = new AtomicInteger(0);
    public static ThreadLocal<Integer> tI = new ThreadLocal<>();

    @Override
    public void run() {
        tI.set(0);
        for (int j = 0; j < 10000000; j++) {
            i++;
            vI++;
            aI.addAndGet(1);
            tI.set(tI.get()+1);
        }
        System.out.println("ThreadLocal " + Thread.currentThread().getName() + ": " + tI.get());
    }

    public static void main(String[] args) throws InterruptedException {

        MutiThreadTest one = new MutiThreadTest();
        MutiThreadTest two = new MutiThreadTest();
        Thread t1 = new Thread( one, "One" );
        Thread t2 = new Thread( two, "Two" );

        t1.start(); t2.start(); // 启动两个线程

        t1.join(); t2.join(); // main线程会等待t1和t2都运行完再执行以后的流程
        System.out.println("Simple: Main: " + i + " One: " + one.i + " Two:" + two.i);
        System.out.println("volatile Main: " + vI + " One: " + one.vI + " Two:" + two.vI);
        System.out.println("AtomicInteger Main: " + aI + " One: " + one.aI + " Two:" + two.aI);
        System.out.println("ThreadLocal Main: " + tI.get() + " One: " + one.tI.get() + " Two:" + two.tI.get());
    }

}
