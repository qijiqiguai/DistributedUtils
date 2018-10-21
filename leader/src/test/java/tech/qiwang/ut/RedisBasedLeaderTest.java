package tech.qiwang.ut;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import tech.qiwang.SpringMain;
import tech.qiwang.core.LeaderI;
import tech.qiwang.impl.RedisBasedLeader;

import java.util.concurrent.TimeUnit;

/**
 * 测试场景
 * 1：获取Leader
 * 2：释放Leader
 * 3：宕机自动释放
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringMain.class)
public class RedisBasedLeaderTest {

    private static final String LEADER_LOCK = "leader";

    @Autowired
    RedisTemplate<String, String> redisTemplate;

//    @Test
    public void basicOps() throws Exception {
        Runnable runnable = () -> {
            // Do Nothing
        };

        LeaderI leader =
                new RedisBasedLeader(redisTemplate, LEADER_LOCK, "Leader1", runnable);
        leader.init();

        leader.tryGetLeader();
        Assert.assertTrue( leader.isLeader() );
        TimeUnit.SECONDS.sleep(10);
        Assert.assertTrue( leader.isLeader() );

        leader.resign();
        Assert.assertTrue( !leader.isLeader() );

        leader.init();
        TimeUnit.SECONDS.sleep(1);
        Assert.assertTrue( leader.isLeader() );

        leader.stopMe();
    }

    @Test
    public void leaderResign() throws Exception {
        Runnable runnable = () -> {
            // Do Nothing
        };

        LeaderI leader1 =
                new RedisBasedLeader(redisTemplate, LEADER_LOCK, "Leader1", runnable);
        leader1.init();

        leader1.tryGetLeader();
        Assert.assertTrue( leader1.isLeader() );
        TimeUnit.SECONDS.sleep(10);

        System.out.println("CurrentLeader: " + leader1.currentLeader());

        LeaderI leader2 =
                new RedisBasedLeader(redisTemplate, LEADER_LOCK, "Leader2", runnable);
        leader2.init();

        leader2.tryGetLeader();
        Assert.assertTrue( !leader2.isLeader() );

        leader1.resign();
        TimeUnit.SECONDS.sleep(10);
        Assert.assertTrue( leader2.isLeader() );
        System.out.println("CurrentLeader: " + leader1.currentLeader());

        leader1.stopMe();
        leader2.stopMe();
    }
}
