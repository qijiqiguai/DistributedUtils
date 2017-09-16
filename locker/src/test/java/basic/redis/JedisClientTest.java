package basic.redis;

import base.redis.JedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import support.Context;


/**
 * Created by wangqi on 2017/9/16 上午11:30.
 */
public class JedisClientTest {
    final static Logger logger = LoggerFactory.getLogger(JedisClientTest.class);
    private final static Context context = new Context();

    public static void main(String[] args) {
        JedisClient client = new JedisClient(
                context.getOneConf("redis.host"),
                Integer.parseInt(context.getOneConf("redis.port"))
        );
        logger.info("服务正在运行: " + client.getClient().ping());
    }
}
