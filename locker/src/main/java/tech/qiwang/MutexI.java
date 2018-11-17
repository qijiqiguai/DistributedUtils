package tech.qiwang;

import java.util.concurrent.TimeUnit;

public interface MutexI {

    /**
     * 在特定时间内尝试获取锁
     * @param time
     * @param unit
     * @return
     */
    boolean acquire(long time, TimeUnit unit);

    /**
     * 获取
     * @return
     */
    boolean acquire();

}
