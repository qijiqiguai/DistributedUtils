package tech.qiwang.demo;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LeaderSelectorDemo {
    private static final String HOST_AND_PORT = "127.0.0.1:2181";
    private static final String LEADER_LOCK_PATH = "/leader";



    @Test
    public void demo() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(
                HOST_AND_PORT,
                new ExponentialBackoffRetry(1000, 3)
        );
        client.start();
        LeaderSelector selector = new LeaderSelector(client, LEADER_LOCK_PATH, new LeaderSelectorListenerAdapter() {

            @Override
            public void takeLeadership(CuratorFramework client) throws Exception {
                System.out.println("Take Leadership");
            }

        });
        selector.autoRequeue();
        selector.start();
        selector.wait();
    }

}
