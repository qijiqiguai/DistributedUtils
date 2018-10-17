package tech.qiwang.impl;


import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import tech.qiwang.MethodNotSupportedException;
import tech.qiwang.core.LeaderI;

import java.io.IOException;


/**
 * http://ifeve.com/zookeeper-leader/
 *
 * 必须启动LeaderLatch: leaderLatch.start(); 一旦启动， LeaderLatch会和其它使用相同latch path的其它LeaderLatch交涉，然后随机的选择其中一个作为leader。
 * 你可以随时查看一个给定的实例是否是leader:
 * public boolean hasLeadership()
 * 一旦不使用LeaderLatch了，必须调用close方法。 如果它是leader,会释放leadership， 其它的参与者将会选举一个leader。
 *
 * ！！！如果此时宕机或关闭，则当前 机器/线程 对应的 zookeeperPath 依然会存活几秒，因此可能出现几秒钟的无主机的状态。
 * // TODO, 查询以下存活几秒，是否可以做到实时响应宕机或关闭
 *
 * 与LeaderLatch相比，通过LeaderSelectorListener可以对领导权进行控制， 在适当的时候释放领导权，这样每个节点都有可能获得领导权。
 * 而LeaderLatch一根筋到死， 除非调用close方法，否则它不会释放领导权。
 */
@Slf4j
public class LeaderLatchBasedLeader implements LeaderI {

    private final LeaderLatch leaderLatch;

    public LeaderLatchBasedLeader(final CuratorFramework client, final String electionPath, final String nodeName) {
        leaderLatch = new LeaderLatch(client, electionPath, nodeName);
    }

    @Override
    public boolean tryGetLeader() {
        if (isLeader()) {
            return true;
        }else {
            throw new MethodNotSupportedException("LeaderLatch do not support actively get leader");
        }
    }

    @Override
    public boolean isLeader() {
        return leaderLatch.hasLeadership();
    }

    @Override
    public boolean resign() throws Exception {
        leaderLatch.close();
        return true;
    }

    @Override
    public void acquireLeaderSuccessCallback() {
        // TODO，发布全局消息
        log.info(leaderLatch.getId() + " Acquire Leader Success: " + leaderLatch.hasLeadership());
    }

    @Override
    public void init() throws Exception {
        LeaderLatchBasedLeader _this = this;
        LeaderLatchListener listener = new LeaderLatchListener(){
            @Override
            public void isLeader() {
                _this.acquireLeaderSuccessCallback();
            }
            @Override
            public void notLeader() {
                // DoNothing
            }
        };
        leaderLatch.addListener(listener);
        leaderLatch.start();
    }

    /**
     * // TODO，需要一个全局高优先通知，可能是直接发送通知给管理员。
     * 当关闭该Leader的时候，如果取消权限失败，那么可能导致其他主机再也无法获取权限了！
     *
     * @throws IOException
     */
    @Override
    public void stopMe() throws Exception {
        resign();
    }
}
