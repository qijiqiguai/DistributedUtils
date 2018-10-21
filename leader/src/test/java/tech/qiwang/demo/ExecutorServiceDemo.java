package tech.qiwang.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(MockitoJUnitRunner.class)
public final class ExecutorServiceDemo {

    @Test
    public void demo() throws Exception {
        ExecutorService service = Executors.newSingleThreadExecutor(r -> new Thread(r, "TestThread"));

        service.execute(() -> {
            while (true) {
                System.out.println("+++++++++++++++++++++");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            System.out.println("Sleep Failed 1");
        }

        service.shutdownNow();

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            System.out.println("Sleep Failed 2");
        }

        // 无法添加，RejectedExecutionException
        service.execute(() -> {
            while (true) {
                System.out.println("---------------------");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("Sleep Failed 3");
                }
            }
        });

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            System.out.println("Sleep Failed 4");
        }
    }

}
