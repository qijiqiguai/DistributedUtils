package tech.qiwang.ut;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.Participant;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import tech.qiwang.MethodNotSupportedException;
import tech.qiwang.impl.LeaderLatchBasedLeader;

import java.io.IOException;
import java.sql.Time;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


/**
 * 测试场景
 * 1：获取Leader
 * 2：释放Leader
 * 3：宕机自动释放
 */
@RunWith(MockitoJUnitRunner.class)
public final class LeaderLatchBasedLeaderTest {
    
//    private static final String HOST_AND_PORT = "127.0.0.1:2180,127.0.0.1:2181,127.0.0.1:2182";
    private static final String HOST_AND_PORT = "127.0.0.1:2181";

    private static final String LEADER_LOCK_PATH = "/leader";

    @Test
    @Ignore
    public void basicOps() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(
                HOST_AND_PORT,
                new ExponentialBackoffRetry(1000, 3)
        );
        client.start();
        client.blockUntilConnected();
        LeaderLatchBasedLeader leader = new LeaderLatchBasedLeader(client, LEADER_LOCK_PATH, "Leader1");
        leader.init();
        TimeUnit.SECONDS.sleep(10);

        Assert.assertTrue( leader.isLeader() );
        Assert.assertTrue( leader.tryGetLeader() );
        TimeUnit.SECONDS.sleep(1);

        leader.resign();
        Assert.assertTrue( !leader.isLeader() );

        try {
            leader.tryGetLeader();
        } catch (Exception e) {
            Assert.assertTrue( e instanceof MethodNotSupportedException);
        }
    }

    @Test
    public void leaderResign() throws Exception {
        CuratorFramework client1 = CuratorFrameworkFactory.newClient(
                HOST_AND_PORT,
                new ExponentialBackoffRetry(1000, 3)
        );
        client1.start();
        client1.blockUntilConnected();
        LeaderLatchBasedLeader leader1 = new LeaderLatchBasedLeader(client1, LEADER_LOCK_PATH, "Leader1");
        leader1.init();
        TimeUnit.SECONDS.sleep(5);

        CuratorFramework client2 = CuratorFrameworkFactory.newClient(
                HOST_AND_PORT,
                new ExponentialBackoffRetry(1000, 3)
        );
        client2.start();
        client2.blockUntilConnected();
        LeaderLatchBasedLeader leader2 = new LeaderLatchBasedLeader(client2, LEADER_LOCK_PATH, "Leader2");
        leader2.init();
        TimeUnit.SECONDS.sleep(5);

        Assert.assertTrue( leader1.isLeader() || leader2.isLeader() );

        if(leader1.isLeader()){
            leader1.resign();
            TimeUnit.SECONDS.sleep(1);
            Assert.assertTrue(leader2.isLeader());
        }else {
            leader2.resign();
            TimeUnit.SECONDS.sleep(1);
            Assert.assertTrue(leader1.isLeader());
        }
    }
}
