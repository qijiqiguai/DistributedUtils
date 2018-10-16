package tech.qiwang.ut;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.Participant;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Assert;
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
    public void assertContend() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(HOST_AND_PORT, new ExponentialBackoffRetry(1000, 3));
        client.start();
        client.blockUntilConnected();
        LeaderLatchBasedLeader leader = new LeaderLatchBasedLeader(client, LEADER_LOCK_PATH, "Leader1");
        leader.init();
        TimeUnit.SECONDS.sleep(1);

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
}
