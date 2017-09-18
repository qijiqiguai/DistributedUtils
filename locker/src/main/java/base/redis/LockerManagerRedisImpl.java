package base.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import support.Constants;
import support.Context;
import support.LockerException;
import support.ServerUtil;

import java.util.Random;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * Created by wangqi on 2017/9/13 上午11:12.
 */

/**
 * 可以选择执行任务采用线程池异步, 但是需要提供Timeout检测。最长持有锁的时间，就是最长的任务执行时间，一旦超时，任务取消，锁释放。
 *
 * 在爬虫任务中，Locker被用来和存储一起执行，那么还是需要知道结果的，否则看上去成功了，实际上超时等等，
 * 一旦操作超时，宁愿认为其失败，否则在执行后续操作（数据抽取）的时候可能就缺失数据。
 * 但是，如果超时还存进去了，而返回的失败，则可能出现存储两次的情况。因此需要提供Rollback方法。
 *
 * 注：由于是分布式的，所以记日志要带ServerName
 */
public class LockerManagerRedisImpl implements LockerManager<Boolean> {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    ExecutorService executor;
    private RedisLocker locker;
    private Jedis jedis;

    {
        Context context = new Context();
        JedisClient client = new JedisClient(
                context.getOneConf("redis.host"),
                Integer.parseInt(context.getOneConf("redis.port"))
        );
        jedis = client.getClient();
        locker = new RedisLocker(client.getClient());

        class LockedJobTF implements ThreadFactory {
            public Thread newThread(Runnable r) {
                return new Thread(r, "Locked-Async-Job");
            }
        }
        executor = Executors.newFixedThreadPool(20, new LockedJobTF());
    }


    private Boolean innerProcess(
            String key, Callable<Boolean> callable, Callable<Boolean> rollback,
            String opsType, boolean needKeyExists, Function<String, Boolean> conditionMatcher,
            String... expected
    ) {
        // 一段时间还没有完成的任务，可以认为已经失败了可以让其他线程尝试, 所以Task的锁需要设置过期时间。
        // 注意这个限制，所以 callable 中的操作不能太久！！！！
        // 加锁的过程，是根据当前Key本身来的生成的一个锁的Key，锁的Key和当前Key是不同的，不用担心冲突
        boolean getLock = locker.lock( Constants.LOCKER_TIMEOUT_MS*2, Constants.LOCKER_TIMEOUT_MS, key);
        if(getLock) {
            try {
                // needKeyExists是否需要判断Key 如果Key都不存在，哪里可能会相等
                // notExistAndDo & existAndDo 不需要在这里判断，后面的主要流程再判断和执行方法
                if(needKeyExists && !jedis.exists(key)){
                    logger.info("Locker:" + opsType + " @ " + ServerUtil.getServerName(),
                            "key-> " + key + " do not exists");
                    return false;
                }

                String current = jedis.get(key);
                Boolean conditionMatches = conditionMatcher.apply(current);
                if( conditionMatches ){
                    //调用线程池执行，这样可以有Timeout的机会
                    Future<Boolean> handler = executor.submit(callable);

                    // 这里很容易出异常，CancellationException、ExecutionException、InterruptedException、TimeoutException
                    // 由于是提交到线程池，所以不能执行完的常见情况是负荷太大，不能按时执行
                    // 根据文档，允许中断的情况下，cancel不能成功的唯一可能就是已经运行完成了。而本身中断也不保证能够真正停止
                    // 所以，当超时的时候唯一的做法就是依赖在处理TimeoutException时进行rollback处理
                    Boolean runRes = handler.get(Constants.LOCKER_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                    // 如果正常执行到这里，失败了也是正常情况，否则会抛异常, 所以就不记日志了
                    return runRes;
                }else {
                    logger.info( "Locker:" + opsType + " @ " + ServerUtil.getServerName(),
                            "condition not match-> key: " + key + " current: " + current + " expected: " + expected
                    );
                }
            } catch (Exception e) {

                // 同步执行，直到完成CallBack, 不以时间限制。潜在风险，其他线程|机器的重试很快完成，这个线程进入了删除步骤，多删除了。
                // 此外还可能出现 状态 Rollback 出现问题，原先保存的原始状态在中途可能被其他线程|机器改掉，导致Rollback会冲掉后面的操作
                // 不过概率极低。Lock时间长的话，很难出现。高并发系统中可能出现
                // 在相关Entity中增加了跟时间相关的唯一标识ID，这样就不存在多删除的风险了。
                // 但是状态更新的Rollback还没有方法可以解决，如果保存了原状态，那么状态可能会变。
                // 如果还加锁会出现循环请求锁的可能！！！，此外效率会进一步下降。
                try {
                    rollback.call(); // 真执行异常了，需要回滚
                } catch (Exception rollbackEx) {
                    logger.info("Locker:"+opsType+"-Exception-Rollback-Fail @ "
                            + ServerUtil.getServerName(), rollbackEx);
                }
                logger.info("Locker:"+opsType+"-Fail-Rollback @ " + ServerUtil.getServerName(), e);
            }finally { // 即使Exception都需要执行锁释放过程
                if (getLock) { // 成功加锁才需要删锁，这样不会对其他锁的情况造成影响。
                    locker.unlock(key);
                }
            }
        } else {
            logger.info("Locker:"+opsType + " @ " +  ServerUtil.getServerName(), "get locker fail");
        }
        return false;
    }

    @Override
    public Boolean equalsAndDo(String key, Callable<Boolean> callable, Callable<Boolean> rollback, String... expected) {
        return innerProcess(  key, callable, rollback,
                "equalsAndDo", true,s -> {
                    boolean equalsExpected = false;
                    for (int i=0; i<expected.length; i++) {
                        if( expected[i].equals(s) ){ // 有一个相等就返回True
                            equalsExpected = true;
                            break;
                        }
                    }
                    return equalsExpected;
                },
                expected
        );
    }

    @Override
    public Boolean notEqualsAndDo(String key, Callable<Boolean> callable,
                                  Callable<Boolean> rollback, String... expected) {
        return innerProcess( key, callable, rollback,
                "notEqualsAndDo", true,s -> {
                    boolean notEqualsExpected = true;
                    for (int i=0; i<expected.length; i++) {
                        if( expected[i].equals(s) ){ //有一个相等就返回False
                            notEqualsExpected = false;
                            break;
                        }
                    }
                    return notEqualsExpected;
                }, expected
        );
    }

    @Override
    public Boolean notExistAndDo(String key, Callable<Boolean> callable,
                                 Callable<Boolean> rollback) {
        return innerProcess( key, callable, rollback,
                "notExistAndDo", false,s -> !jedis.exists(key), // 不存在就返回True
                null
        );
    }

    @Override
    public Boolean existAndDo(String key,
                              Callable<Boolean> callable, Callable<Boolean> rollback) {
        return innerProcess( key, callable, rollback,
                "existAndDo", false,s -> jedis.exists(key), // 存在就返回True
                null
        );
    }
}

class RedisLocker {
    private static final String KEY_PREFIX = "DISTRIBUTE-LOCKER:";
    private Jedis jedis;

    public RedisLocker(Jedis jedis) {
        this.jedis = jedis;
    }
    /**
     * 加锁
     * 使用方式为：
     * boolean locked = lock();
     * if(locked){
     *   try{
     *     executeMethod();
     *   }finally{
     *      if(locked) {
     *          unlock();
     *      }
     *   }
     * }
     *
     * @param tryMs timeout的时间范围内轮询锁, 单位为毫秒, 由于这个进度，不建议在秒杀等超高并发项目中使用
     * @param expireMs 设置锁超时时间，单位为毫秒
     * @return 成功 or 失败
     */
    public boolean lock(long tryMs, long expireMs, String key){
        long startTime = System.currentTimeMillis();
        try {
            //在timeout的时间范围内不断轮询锁，轮询一段时间还没有拿到锁，就失败退出
            while (System.currentTimeMillis() - startTime < tryMs) {
                //锁不存在的话，设置锁并设置锁过期时间，即加锁
                if (this.jedis.setnx(KEY_PREFIX + key, "0") == 1) {
                    //设置锁过期时间是为了在没有释放锁的情况下锁过期后消失，不会造成永久阻塞
                    this.jedis.pexpire(KEY_PREFIX + key, expireMs);
                    return true;
                }
                //短暂休眠，避免可能的活锁
                Thread.sleep(0, new Random().nextInt(100));
            }
        } catch (Exception e) {
            throw new LockerException("lock redis key error: " + key + " @ "
                    + ServerUtil.getServerName(), e);
        }
        return false;
    }

    public void unlock(String key) {
        try {
            jedis.del(KEY_PREFIX + key);//直接删除
        } catch (Exception e) {
            throw new LockerException("unlock redis key error: " + key + " @ "
                    +  ServerUtil.getServerName(), e);
        }
    }
}
