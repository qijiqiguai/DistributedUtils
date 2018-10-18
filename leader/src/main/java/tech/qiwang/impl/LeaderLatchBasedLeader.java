package tech.qiwang.impl;


import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import tech.qiwang.MethodNotSupportedException;
import tech.qiwang.core.LeaderI;
import java.io.IOException;


/**
 * 必须启动LeaderLatch: leaderLatch.start(); 一旦启动， LeaderLatch会和其它使用相同latch path的其它LeaderLatch交涉，然后随机的选择其中一个作为leader。
 * 你可以随时查看一个给定的实例是否是leader:
 * public boolean hasLeadership()
 * 一旦不使用LeaderLatch了，必须调用close方法。 如果它是leader,会释放leadership， 其它的参与者将会选举一个leader。
 * LeaderLatch是拿到权限就不放， 直到调用close方法或者关闭。
 * 这样我们能获得一个相对稳定的主机。
 *
 * ！！！如果此时宕机或关闭，则当前 机器/线程 对应的 zookeeperPath 依然会存活几秒，因此可能出现几秒钟的无主机的状态。
 * // TODO, 查询以下存活几秒，是否可以做到实时响应宕机或关闭
 *
 * LeaderSelectorListener接口中最主要的方法就是takeLeadership方法，Curator会在竞争到Master后自动调用该方法，开发者可以在这个方法中实现自己的业务逻辑。
 * !!! 需要注意的一点是，一旦执行完takeLeadership方法，Curator就会立即释放Master权利，然后重新开始新一轮的Master选举。
 * 即Leader权限是在各各节点之间始终竞争的，争到了就执行一次，然后进入下一次竞争。
 * 这对于需要平均分配在各个机器之间、且需要持续执行的任务是很好的。
 *
 * 但是，这对于使用者的心智来说，每台机器看上去都在执行任务，只是获取了Leader的机器在 takeLeadership 中才真正执行，其余的都是假象。
 * 这个思路跟现在的设计是不一致的，现在的设计是在执行任务处判断是否是Leader来确认是否真的可以执行，这个过程对开发者来说是显性的。
 * 两者没有好坏之别，只是思路不一样。本处设计比较偏好于固定主机，觉得过程可控。
 *
 * 从维护的角度来说，相对稳定的主机有相对有据可循的日志，远程调试信息也比较容易获取。
 * 而，随机分布的主机，则能自动规避单一主机的性能瓶颈问题。单一主机的则需要自己监控性能来主动释放权限。
 */
@Slf4j
public class LeaderLatchBasedLeader implements LeaderI {
    private final LeaderLatch leaderLatch;
    private final Runnable callback;

    public LeaderLatchBasedLeader(final CuratorFramework client, final String electionPath, final String nodeName, Runnable callback) {
        leaderLatch = new LeaderLatch(client, electionPath, nodeName);

        LeaderLatchListener listener = new LeaderLatchListener(){
            @Override
            public void isLeader() {
                acquireLeaderSuccessCallback();
            }
            @Override
            public void notLeader() {
                // DoNothing
            }
        };
        leaderLatch.addListener(listener);

        this.callback = callback;
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
        callback.run();
    }

    @Override
    public void init() throws Exception {
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
        leaderLatch.close();
    }
}
