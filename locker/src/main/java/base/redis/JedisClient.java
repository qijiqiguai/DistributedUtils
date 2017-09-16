package base.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by wangqi on 2017/9/16 上午11:21.
 */
public class JedisClient {
    JedisPoolConfig config;
    JedisPool jedisPool;
    String host;
    int port;

    public JedisClient(String host, int port){
        config = new JedisPoolConfig();
        config.setBlockWhenExhausted(true);
        config.setJmxEnabled(true);
        config.setJmxNamePrefix("JedisClient");
        jedisPool = new JedisPool(config, host, port );
    }

    public Jedis getClient() {
        return jedisPool.getResource();
    }
}
