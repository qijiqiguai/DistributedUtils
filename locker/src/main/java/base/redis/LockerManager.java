package base.redis;

import java.util.concurrent.Callable;

/**
 * Created by wangqi on 2017/9/13 上午11:11.
 */
public interface LockerManager<R> {

    R equalsAndDo(String key, Callable<R> callable, Callable<R> rollback, String... expected);

    R notEqualsAndDo(String key, Callable<R> callable, Callable<R> rollback, String... expected);

    R notExistAndDo(String key, Callable<R> callable, Callable<R> rollback);

    R existAndDo( String key, Callable<R> callable, Callable<R> rollback);
}
