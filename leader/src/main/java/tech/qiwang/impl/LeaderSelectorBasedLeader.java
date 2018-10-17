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
 * LeaderSelectorListener接口中最主要的方法就是takeLeadership方法，Curator会在竞争到Master后自动调用该方法，开发者可以在这个方法中实现自己的业务逻辑。
 * !!! 需要注意的一点是，一旦执行完takeLeadership方法，Curator就会立即释放Master权利，然后重新开始新一轮的Master选举。
 * 即Leader权限是在各各节点之间始终竞争的，争到了就执行一次，然后进入下一次竞争。
 * 这对于需要平均分配在各个机器之间、且需要持续执行的任务是很好的。
 */
@Slf4j
public class LeaderSelectorBasedLeader implements LeaderI {


    @Override
    public boolean tryGetLeader() throws Exception {
        return false;
    }

    @Override
    public boolean isLeader() {
        return false;
    }

    @Override
    public boolean resign() throws Exception {
        return false;
    }

    @Override
    public void acquireLeaderSuccessCallback() {

    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public void stopMe() throws Exception {

    }
}
