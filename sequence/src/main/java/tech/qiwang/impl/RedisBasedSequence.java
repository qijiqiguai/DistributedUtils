package tech.qiwang.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import tech.qiwang.SequenceI;

import java.util.Random;
import java.util.concurrent.TimeUnit;


@Slf4j
public class RedisBasedSequence implements SequenceI {
    private final RedisTemplate<String, String> redisTemplate;
    private final String key;

    public RedisBasedSequence(RedisTemplate<String, String> template, String redisKey ){
        this.redisTemplate = template;
        this.key = redisKey;
    }

    @Override
    public Boolean init(long start) {
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.set(key.getBytes(), (start+"").getBytes());
            }
        });
    }

    @Override
    public Long numberSequence() {
        Long result = redisTemplate.execute(
            (RedisCallback<Long>) connection -> connection.incr(key.getBytes())
        );
        return result;
    }


    /**
     * 10位时间 + X + 4位秒内计数器 + 随机数
     * @return
     */
    public String generateSerialNum() {
        String timeStr = String.valueOf( System.currentTimeMillis() ).substring(0, 10);
        String random = String.valueOf(100 + new Random().nextInt((999 - 100) + 1));

        // 4位核心订单号
        long result = generateOrderCountStr("orderCount");
        Assert.isTrue(result<10000, "一秒钟超过10000, 生成失败");
        String redisValue = "X" + String.valueOf(result + 10000L).substring(1);

        return timeStr + redisValue + random;
    }

    private long generateOrderCountStr(String key) {
        Long result = redisTemplate.execute(
                (RedisCallback<Long>) connection -> connection.incr(key.getBytes())
        );
        if(null != result) {
            if(result == 1) {
                redisTemplate.expire(key, 1, TimeUnit.SECONDS);
                return result;
            }
            if(result > 1) {
                return result;
            }
        }
        return -1;
    }

}
