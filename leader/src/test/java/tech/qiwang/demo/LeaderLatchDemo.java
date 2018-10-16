package tech.qiwang.demo;

import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.Participant;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(MockitoJUnitRunner.class)
public final class LeaderLatchDemo {
    
//    private static final String HOST_AND_PORT = "127.0.0.1:2180,127.0.0.1:2181,127.0.0.1:2182";
    private static final String HOST_AND_PORT = "127.0.0.1:2181";

    private static final String LEADER_LOCK_PATH = "/leader";

    @Test
    public void assertContend() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(HOST_AND_PORT, new ExponentialBackoffRetry(1000, 3));
        client.start();

        List<LeaderLatch> latchList = IntStream.rangeClosed(1,10)
                .parallel()
                .mapToObj(i -> new LeaderLatch(client, LEADER_LOCK_PATH,"client"+i))
                .collect(Collectors.toList());

        latchList.parallelStream()
                .forEach(latch -> {
                    try {
                        latch.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        TimeUnit.SECONDS.sleep(5);

        Iterator<LeaderLatch> iterator = latchList.iterator();
        while (iterator.hasNext()){
            LeaderLatch latch = iterator.next();
            if(latch.hasLeadership()){
                System.out.println(latch.getId() + " hasLeadership");
                try {
                    latch.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                iterator.remove();
            }
        }


        TimeUnit.SECONDS.sleep(5);

        latchList.stream()
                .filter(latch -> latch.hasLeadership())
                .forEach(latch -> System.out.println(latch.getId() + " hasLeadership"));

        Participant participant = latchList.get(0).getLeader();
        System.out.println(participant);


        TimeUnit.MINUTES.sleep(15);

        latchList.stream()
                .forEach(latch -> {
                    try {
                        latch.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        client.close();
    }
}
