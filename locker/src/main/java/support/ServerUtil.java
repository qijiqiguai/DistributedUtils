package support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by wangqi on 2017/9/13 下午2:55.
 */
public class ServerUtil {
    protected static final Logger logger = LoggerFactory.getLogger(ServerUtil.class);

    public static String getServerName() {
        try {
            return InetAddress.getLocalHost().toString() + " @ " + System.currentTimeMillis();
        } catch (UnknownHostException e1) {
            logger.warn("UnknownHost @ " + System.currentTimeMillis());
            return "UnknownHost @ " + System.currentTimeMillis();
        }
    }
}
