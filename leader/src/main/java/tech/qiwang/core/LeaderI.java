package tech.qiwang.core;


import tech.qiwang.Initable;
import tech.qiwang.Stoppable;

/**
 * 存在众多需要指定主机的场景
 * 1：任务分发
 * 2：定时任务
 * 3：通知
 * 等
 */
public interface LeaderI extends Initable, Stoppable {

    /**
     * 返回当前Leader的标识
     * @return
     */
    String currentLeader() throws Exception;

    /**
     * 尝试获取Leader权限
     * @return
     */
    boolean tryGetLeader() throws Exception;

    /**
     * 当前节点是否是Leader
     * @return
     */
    boolean isLeader();


    /**
     * 尝试取消本机的Leader权限
     * 如果当前是Leader且取消成功则返回true，如果不是Leader也返回true
     *
     * 常用于主机关机，关闭之前主动释放权限。
     *
     * 有两种可能性
     * 1：释放的同时中间件比如ZK会通知其他监听节点，其他节点加入竞争
     * 2：释放时不会通知其他节点，比如普通的RedisKey，则其他节点需要轮询当前Leader状态并进行竞争
     * @return
     */
    boolean resign() throws Exception;

    void acquireLeaderSuccessCallback();
}
