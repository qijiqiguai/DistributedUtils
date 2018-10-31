package tech.qiwang;

import cn.hutool.core.util.IdUtil;


/**
 * https://www.cnblogs.com/haoxinyue/p/5208136.html
 */
public interface SequenceI {

    /**
     * 如果生成的ID是基于全局数字的，那么从某个数字开始
     * 初值小于0，则不做设置，即保留原值。重启时有用
     * @param start
     * @return
     */
    Boolean init(long start);

    /**
     * 采用MongoDB的ID生成策略，这样前四位会隐藏时间信息
     * 此外，根据测试100W生成的结果，ObjectID生成速度比UUID要快
     * @return
     */
    default String stringUid(){
        return IdUtil.objectId();
    }

    /**
     * 连续性自增数字
     * @return
     */
    Long numberSequence();


    /**
     * CurrentTimeMillis + Server标记 + 时间段内自增数
     * @return
     */
    default Long snowflakeId(long workerId, long dcId) {
        return IdUtil.createSnowflake(workerId, dcId).nextId();
    }

}
