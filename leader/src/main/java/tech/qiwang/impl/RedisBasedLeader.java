package tech.qiwang.impl;


import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import tech.qiwang.core.LeaderI;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


/**
 * Zookeeper生产线至少部属三个节点，有一定维护成本的，一般单体系统中没有必要部属
 * 而Redis基本上是系统标配，在极简主义的角度下，需要基于Redis构建Leader选择器
 */
@Slf4j
public class RedisBasedLeader implements LeaderI {
    private final RedisTemplate<String, String> redisTemplate;
    private final Runnable callback;
    private final String electionPath;
    private final String nodeName;
    private final int LEADER_LIFE_SECONDS = 10;


    public RedisBasedLeader(final RedisTemplate redisTemplate, final String electionPath, final String nodeName, Runnable callback) {
        this.redisTemplate = redisTemplate;
        this.electionPath = electionPath;
        this.callback = callback;
        this.nodeName = nodeName;
    }

    @Override
    public boolean tryGetLeader() {
        boolean result = redisTemplate.execute(
            (RedisCallback<Boolean>) connection -> connection.setNX(electionPath.getBytes(), nodeName.getBytes())
        );
        if(result) {
            // LEADER_LIFE_SECONDS 秒内，Leader有效，如果刚好获取了Leader之后，就挂了，那么有 LEADER_LIFE_SECONDS 秒是没有Leader的
            redisTemplate.expire(electionPath, LEADER_LIFE_SECONDS, TimeUnit.SECONDS);
            acquireLeaderSuccessCallback();
        }else {
            log.debug("Acquire Leader Fail");
        }
        return result;
    }

    @Override
    public boolean isLeader() {
        String currentLeader = getStringValue(electionPath);
        log.info("Current Leader: " + currentLeader);
        if( null == currentLeader ){
            // 当前没有Leader的情况下，自己主动争取做Leader。
            return tryGetLeader();
        }else {
            if(currentLeader.equals(nodeName)) {
                return true;
            }else {
                return false;
            }
        }
    }

    @Override
    public boolean resign() throws Exception {
        String currentLeader = getStringValue(electionPath);
        // 如果当前就是Leader
        if( null!=currentLeader || currentLeader.equals(nodeName) ){
            redisTemplate.delete(electionPath);
        }
        return true;
    }

    @Override
    public void acquireLeaderSuccessCallback() {
        log.debug(nodeName + " Acquire Leader Success");
        callback.run();
    }

    @Override
    public void init() throws Exception {
        Timer timer = new Timer();
        //前一次执行程序结束后 1S 后开始执行下一次程序
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                tryGetLeader();
            }
        }, 0,1000);
    }

    /**
     * // TODO，需要一个全局高优先通知，可能是直接发送通知给管理员。
     *
     * @throws IOException
     */
    @Override
    public void stopMe() throws Exception {
        resign();
    }


    public String getStringValue(String key) {
        byte[] result = redisTemplate.execute(
                (RedisCallback<byte[]>) connection -> connection.get(key.getBytes())
        );
        if(result == null || result.length==0) {
            return null;
        }else {
            return new String(result);
        }
    }
}
