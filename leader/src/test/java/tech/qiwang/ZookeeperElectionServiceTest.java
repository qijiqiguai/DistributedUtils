package tech.qiwang;

import org.junit.Assert;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import tech.qiwang.impl.LeaderLatchBasedLeader;

@RunWith(MockitoJUnitRunner.class)
public final class ZookeeperElectionServiceTest {
    
    private static final String HOST_AND_PORT = "localhost:2181";
    
    private static final String ELECTION_PATH = "/election";

    @Test
    public void assertContend() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181", new RetryOneTime(2000));
        client.start();
        client.blockUntilConnected();
        LeaderLatchBasedLeader service = new LeaderLatchBasedLeader(client, ELECTION_PATH, System.currentTimeMillis() + "");
        Thread.sleep(2000);
        Assert.assertTrue( service.isLeader() );
    }
}
